package com.veil.app.services

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import com.veil.app.core.SessionDeadline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

data class CommitmentSnapshot(
    val onboarded: Boolean = false,
    val focusPackages: Set<String> = emptySet(),
    val focusStartElapsed: Long = 0,
    val focusEndElapsed: Long = 0,
    val cooldownPackage: String = "",
    val cooldownEndElapsed: Long = 0,
    val cooldownMinutes: Int = 5,
    val bootCount: Int = -1,
    val dark: Boolean = false,
    val events: List<LocalEvent> = emptyList(),
) {
    fun focusRemaining(now: Long, boot: Int) = SessionDeadline(focusEndElapsed, bootCount).remaining(now, boot)
    fun cooldownRemaining(now: Long, boot: Int) = SessionDeadline(cooldownEndElapsed, bootCount).remaining(now, boot)
}

data class LocalEvent(val kind: String, val at: Long)

/** No content, frames, URLs or foreground app history. Only selected rules and bounded event kinds. */
class CommitmentStore private constructor(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("veil_commitments", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(read())
    val state: StateFlow<CommitmentSnapshot> = mutable
    private val connected = MutableStateFlow(false)
    val accessibilityConnected: StateFlow<Boolean> = connected
    fun connection(active: Boolean) { connected.value = active }
    fun bootCount(): Int = Settings.Global.getInt(app.contentResolver, Settings.Global.BOOT_COUNT, -1)

    @Synchronized private fun change(transform: (CommitmentSnapshot) -> CommitmentSnapshot) {
        val s = transform(mutable.value)
        val events = s.events.filter { it.at >= System.currentTimeMillis() - RETENTION_MS }.takeLast(100)
        val next = s.copy(events = events)
        val json = JSONObject().put("onboarded", next.onboarded).put("packages", JSONArray(next.focusPackages.toList()))
            .put("start", next.focusStartElapsed).put("end", next.focusEndElapsed)
            .put("cooldownPackage", next.cooldownPackage).put("cooldownEnd", next.cooldownEndElapsed)
            .put("minutes", next.cooldownMinutes).put("boot", next.bootCount).put("dark", next.dark)
            .put("events", JSONArray().also { a -> events.forEach { a.put(JSONObject().put("kind", it.kind).put("at", it.at)) } })
        prefs.edit().putString("state", json.toString()).apply()
        mutable.value = next
    }

    private fun read(): CommitmentSnapshot = runCatching {
        val j = JSONObject(prefs.getString("state", "{}") ?: "{}")
        val packages = j.optJSONArray("packages") ?: JSONArray()
        val events = j.optJSONArray("events") ?: JSONArray()
        CommitmentSnapshot(j.optBoolean("onboarded"), (0 until packages.length()).map { packages.getString(it) }.toSet(),
            j.optLong("start"), j.optLong("end"), j.optString("cooldownPackage"), j.optLong("cooldownEnd"),
            j.optInt("minutes", 5).coerceIn(5, 15), j.optInt("boot", -1), j.optBoolean("dark"),
            (0 until events.length()).map { events.getJSONObject(it).let { e -> LocalEvent(e.getString("kind"), e.getLong("at")) } }
                .filter { it.at >= System.currentTimeMillis() - RETENTION_MS }.takeLast(100))
    }.getOrDefault(CommitmentSnapshot())

    fun onboard() = change { it.copy(onboarded = true) }
    fun theme(dark: Boolean) = change { it.copy(dark = dark) }
    fun cooldownMinutes(minutes: Int) { require(minutes in setOf(5, 10, 15)); change { it.copy(cooldownMinutes = minutes) } }
    fun startFocus(packages: Set<String>, minutes: Int, delayMinutes: Int = 0) {
        require(packages.isNotEmpty() && minutes in 1..10080 && delayMinutes in 0..1440)
        require(packages.none { isEssentialPackage(app, it) })
        val start = SystemClock.elapsedRealtime() + delayMinutes * 60_000L
        change { it.copy(focusPackages = packages.toSet(), focusStartElapsed = start,
            focusEndElapsed = start + minutes * 60_000L, bootCount = bootCount(), events = it.events + event("FOCUS_CREATED")) }
    }
    fun endFocus() = change { it.copy(focusEndElapsed = 0, focusPackages = emptySet(), events = it.events + event("FOCUS_ENDED_EARLY")) }
    fun intervene(score: Float, packageName: String) {
        if (!score.isFinite() || !connected.value || isEssentialPackage(app, packageName)) return
        change {
            if (it.cooldownRemaining(SystemClock.elapsedRealtime(), bootCount()) > 0) it
            else it.copy(cooldownPackage = packageName, cooldownEndElapsed = SystemClock.elapsedRealtime() + it.cooldownMinutes * 60_000L,
                bootCount = bootCount(), events = it.events + event("AI_INTERVENTION"))
        }
    }
    fun dismissCooldown(incorrect: Boolean = false) = change { it.copy(cooldownPackage = "", cooldownEndElapsed = 0,
        events = it.events + event(if (incorrect) "FALSE_POSITIVE" else "EMERGENCY_EXIT")) }
    fun recoveryComplete() = change { it.copy(events = it.events + event("RECOVERY_SELF_REPORTED")) }
    fun clearHistory() = change { it.copy(events = emptyList()) }
    fun clearAll() = change { CommitmentSnapshot(onboarded = true, dark = it.dark) }
    fun tick() {
        if (mutable.value.events.any { it.at < System.currentTimeMillis() - RETENTION_MS }) change { it }
        val s = mutable.value
        val now = SystemClock.elapsedRealtime()
        val boot = bootCount()
        if (s.bootCount != boot && (s.focusEndElapsed > 0 || s.cooldownEndElapsed > 0)) {
            change { it.copy(focusEndElapsed = 0, focusPackages = emptySet(), cooldownEndElapsed = 0, cooldownPackage = "", bootCount = boot,
                events = it.events + event("REBOOT_SESSIONS_ENDED")) }
        } else {
            if (s.focusEndElapsed > 0 && s.focusRemaining(now, boot) == 0L) change { it.copy(focusEndElapsed = 0, focusPackages = emptySet(), events = it.events + event("FOCUS_COMPLETED")) }
            if (s.cooldownEndElapsed > 0 && s.cooldownRemaining(now, boot) == 0L) change { it.copy(cooldownEndElapsed = 0, cooldownPackage = "", events = it.events + event("COOLDOWN_COMPLETED")) }
        }
    }
    fun blockingReason(packageName: String): String? {
        if (isEssentialPackage(app, packageName)) return null
        val s = mutable.value
        val now = SystemClock.elapsedRealtime()
        return when {
            s.cooldownPackage == packageName && s.cooldownRemaining(now, bootCount()) > 0 -> "Take back control. Your cooldown is active."
            packageName in s.focusPackages && now >= s.focusStartElapsed && s.focusRemaining(now, bootCount()) > 0 -> "Give your goal some room. Your focus session is active."
            else -> null
        }
    }
    private fun event(kind: String) = LocalEvent(kind, System.currentTimeMillis())
    companion object {
        private const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000
        @Volatile private var instance: CommitmentStore? = null
        fun get(context: Context): CommitmentStore = instance ?: synchronized(this) { instance ?: CommitmentStore(context).also { instance = it } }
    }
}

fun isEssentialPackage(context: Context, name: String): Boolean {
    val home = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME)
    val launcher = context.packageManager.resolveActivity(home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    val dialer = context.getSystemService(android.telecom.TelecomManager::class.java)?.defaultDialerPackage
    return name.isBlank() || name == context.packageName || name == launcher || name == dialer ||
        name.startsWith("com.android.") || name.contains("permissioncontroller") || name.contains("launcher") ||
        name.contains("incallui") || name.contains("dialer") || name.contains("emergency") ||
        name.contains("settings") || name == "android" || name.contains("systemui")
}
