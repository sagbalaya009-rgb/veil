package com.veil.app.services

import android.content.Context
import android.os.SystemClock
import com.veil.app.core.Correction
import com.veil.app.core.FeedbackEntry
import com.veil.app.core.FeedbackPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Versioned local corrections: no pixels, package names, URLs, embeddings or model mutations. */
class FeedbackStore internal constructor(context: Context, private val preferenceName: String = "veil_feedback_v1") {
    private val prefs = context.applicationContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    private val mutableAvailable = MutableStateFlow(true)
    val storageAvailable: StateFlow<Boolean> = mutableAvailable
    private val mutable = MutableStateFlow(read())
    val state: StateFlow<List<FeedbackEntry>> = mutable
    private var undoId: String? = null
    private var undoUntil = 0L
    init { if (mutableAvailable.value) persist(mutable.value) }
    private fun read(): List<FeedbackEntry> = runCatching {
        val j = JSONObject(prefs.getString("state", "{}") ?: "{}")
        require(j.optInt("schema", 1) == 1)
        val rows = j.optJSONArray("entries") ?: JSONArray()
        FeedbackPolicy.retained((0 until rows.length()).map { i -> rows.getJSONObject(i).let {
            FeedbackEntry(it.getString("id"), it.getLong("at"), Correction.valueOf(it.getString("correction")), it.getString("model"))
        } }, System.currentTimeMillis())
    }.getOrElse { mutableAvailable.value = false; emptyList() }
    @Synchronized private fun persist(entries: List<FeedbackEntry>): Boolean {
        if (!mutableAvailable.value) return false
        val next = FeedbackPolicy.retained(entries, System.currentTimeMillis())
        val j = JSONObject().put("schema", 1).put("entries", JSONArray().also { rows -> next.forEach {
            rows.put(JSONObject().put("id", it.id).put("at", it.at).put("correction", it.correction.name).put("model", it.model))
        } })
        if (!prefs.edit().putString("state", j.toString()).commit()) { mutableAvailable.value = false; return false }
        mutable.value = next
        return true
    }
    @Synchronized fun report(correction: Correction): Boolean {
        val entry = FeedbackEntry(UUID.randomUUID().toString(), System.currentTimeMillis(), correction, VeilModelSpec.version)
        if (!persist(mutable.value + entry)) return false
        undoId = entry.id; undoUntil = SystemClock.elapsedRealtime() + 10_000
        return true
    }
    @Synchronized fun undo(): Boolean {
        val id = undoId ?: return false
        if (SystemClock.elapsedRealtime() > undoUntil) { undoId = null; return false }
        val saved = persist(mutable.value.filterNot { it.id == id }); undoId = null; return saved
    }
    @Synchronized fun clear(): Boolean {
        val deleted = prefs.edit().clear().commit()
        mutableAvailable.value = deleted
        if (deleted) mutable.value = emptyList()
        undoId = null
        return deleted
    }
    companion object {
        @Volatile private var instance: FeedbackStore? = null
        fun get(context: Context): FeedbackStore = instance ?: synchronized(this) {
            instance ?: FeedbackStore(context).also { instance = it }
        }
    }
}
