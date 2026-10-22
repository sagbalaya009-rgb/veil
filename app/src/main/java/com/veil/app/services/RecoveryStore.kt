package com.veil.app.services

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.veil.app.core.recovery.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Recovery data is separate from diagnostics, prediction policy, and the legacy Room notes. */
class RecoveryStore internal constructor(context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "veil_recovery_v1.enc"))
    private val mutableState = MutableStateFlow(read())
    val state: StateFlow<RecoveryState> = mutableState.asStateFlow()

    init {
        // Remove expired records from disk on cold open, not just from the visible state.
        if (mutableState.value.storageAvailable && file.baseFile.exists()) change { it }
    }

    @Synchronized private fun change(update: (RecoveryState) -> RecoveryState): Boolean {
        val current = mutableState.value
        if (!current.storageAvailable) return false
        val next = update(current).let { it.copy(records = RecoveryRules.retain(it.records, System.currentTimeMillis())) }
        return try {
            write(next)
            mutableState.value = next
            true
        } catch (_: Exception) {
            mutableState.value = current.copy(storageAvailable = false, storageMessage = STORAGE_ERROR)
            false
        }
    }

    fun configure(goals: Set<RecoveryGoal>, encouragement: EncouragementMode, customReminder: String = ""): Boolean = change {
        it.copy(goals = goals.toSet(), encouragement = encouragement,
            customReminder = customReminder.trim().take(RecoveryRules.MAX_REMINDER_LENGTH))
    }

    fun savePlan(trigger: String, action: String): Boolean = change {
        it.copy(planTrigger = trigger.trim().take(RecoveryRules.MAX_PLAN_LENGTH), planAction = action.trim().take(RecoveryRules.MAX_PLAN_LENGTH))
    }

    fun logCheckIn(intensity: Int? = null, trigger: Trigger? = null, note: String = ""): String? {
        require(intensity == null || intensity in 0..10)
        // Skipping every optional field records nothing.
        if (intensity == null && trigger == null && note.isBlank()) return null
        val record = RecoveryRecord(UUID.randomUUID().toString(), System.currentTimeMillis(), RecoveryRecordKind.CHECK_IN,
            intensity = intensity, trigger = trigger, note = note.trim().take(RecoveryRules.MAX_NOTE_LENGTH))
        return record.id.takeIf { change { RecoveryRules.add(it, record, record.atEpochMillis) } }
    }

    fun completeActivity(activity: RecoveryActivity, helpful: Boolean? = null): String? {
        val record = RecoveryRecord(UUID.randomUUID().toString(), System.currentTimeMillis(), RecoveryRecordKind.ACTIVITY,
            activity = activity, helpful = helpful)
        return record.id.takeIf { change { RecoveryRules.add(it, record, record.atEpochMillis) } }
    }

    fun reportSetback(): Boolean {
        val record = RecoveryRecord(UUID.randomUUID().toString(), System.currentTimeMillis(), RecoveryRecordKind.SETBACK)
        return change { RecoveryRules.add(it, record, record.atEpochMillis) }
    }

    fun setHelpful(id: String, helpful: Boolean?): Boolean = change { current ->
        current.copy(records = current.records.map { if (it.id == id && it.kind == RecoveryRecordKind.ACTIVITY) it.copy(helpful = helpful) else it })
    }

    fun deleteRecord(id: String): Boolean = change { it.copy(records = it.records.filterNot { record -> record.id == id }) }
    fun deleteRecords(): Boolean = change { it.copy(records = emptyList()) }
    fun deletePreferences(): Boolean = change { RecoveryState(records = it.records) }

    /** Explicit deletion remains possible even if the encryption key or ciphertext is unavailable. */
    @Synchronized fun deleteAll(): Boolean = try {
        file.delete()
        check(!file.baseFile.exists())
        mutableState.value = RecoveryState()
        true
    } catch (_: Exception) {
        mutableState.value = mutableState.value.copy(storageAvailable = false, storageMessage = STORAGE_ERROR)
        false
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }

    private fun write(state: RecoveryState) {
        val plain = encode(state).toString().toByteArray(Charsets.UTF_8)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()); updateAAD(AAD) }
            val encrypted = cipher.doFinal(plain)
            val stream = file.startWrite()
            try {
                stream.write(byteArrayOf(1, cipher.iv.size.toByte()))
                stream.write(cipher.iv)
                stream.write(encrypted)
                file.finishWrite(stream)
            } catch (error: Exception) { file.failWrite(stream); throw error }
        } finally { plain.fill(0) }
    }

    private fun read(): RecoveryState {
        if (!file.baseFile.exists()) return RecoveryState()
        return try {
            val bytes = file.openRead().use { stream ->
                // Bound encrypted input before JSON parsing; private files may still be corrupted.
                val buffer = ByteArray(MAX_FILE_BYTES + 1)
                var total = 0
                while (total < buffer.size) {
                    val count = stream.read(buffer, total, buffer.size - total)
                    if (count < 0) break
                    total += count
                }
                require(total <= MAX_FILE_BYTES)
                buffer.copyOf(total).also { buffer.fill(0) }
            }
            require(bytes.size >= 30 && bytes[0].toInt() == 1 && bytes[1].toInt() == 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(2, 14))); updateAAD(AAD)
            }
            val plain = cipher.doFinal(bytes, 14, bytes.size - 14)
            try { decode(JSONObject(String(plain, Charsets.UTF_8))) } finally { plain.fill(0) }
        } catch (_: Exception) { RecoveryState(storageAvailable = false, storageMessage = STORAGE_ERROR) }
    }

    private fun encode(state: RecoveryState) = JSONObject().apply {
        put("schema", 1)
        put("goals", JSONArray(state.goals.map { it.name }))
        put("encouragement", state.encouragement.name)
        put("reminder", state.customReminder)
        put("planTrigger", state.planTrigger)
        put("planAction", state.planAction)
        put("records", JSONArray().apply { state.records.forEach { record -> put(JSONObject().apply {
            put("id", record.id); put("at", record.atEpochMillis); put("kind", record.kind.name)
            record.intensity?.let { put("intensity", it) }; record.trigger?.let { put("trigger", it.name) }
            put("note", record.note); record.activity?.let { put("activity", it.name) }; record.helpful?.let { put("helpful", it) }
        }) } })
    }

    private fun decode(json: JSONObject): RecoveryState {
        require(json.getInt("schema") == 1)
        val goals = json.getJSONArray("goals")
        val rows = json.getJSONArray("records")
        require(goals.length() <= RecoveryGoal.entries.size && rows.length() <= RecoveryRules.MAX_RECORDS)
        val state = RecoveryState(
            goals = (0 until goals.length()).map { RecoveryGoal.valueOf(goals.getString(it)) }.toSet(),
            encouragement = EncouragementMode.valueOf(json.getString("encouragement")),
            customReminder = json.optString("reminder").take(RecoveryRules.MAX_REMINDER_LENGTH),
            planTrigger = json.optString("planTrigger").take(RecoveryRules.MAX_PLAN_LENGTH),
            planAction = json.optString("planAction").take(RecoveryRules.MAX_PLAN_LENGTH),
        )
        val now = System.currentTimeMillis()
        return (0 until rows.length()).fold(state) { current, index ->
            val row = rows.getJSONObject(index)
            RecoveryRules.add(current, RecoveryRecord(
                id = row.getString("id"), atEpochMillis = row.getLong("at"), kind = RecoveryRecordKind.valueOf(row.getString("kind")),
                intensity = if (row.has("intensity")) row.getInt("intensity") else null,
                trigger = if (row.has("trigger")) Trigger.valueOf(row.getString("trigger")) else null,
                note = row.optString("note").take(RecoveryRules.MAX_NOTE_LENGTH),
                activity = if (row.has("activity")) RecoveryActivity.valueOf(row.getString("activity")) else null,
                helpful = if (row.has("helpful")) row.getBoolean("helpful") else null,
            ), now)
        }
    }

    companion object {
        private const val KEY_ALIAS = "veil_recovery_v1"
        private const val MAX_FILE_BYTES = 512 * 1024
        private val AAD = "Veil Recovery schema 1".toByteArray(Charsets.UTF_8)
        private const val STORAGE_ERROR = "Recovery storage is unavailable. Existing records were preserved. You can delete Recovery data in Settings."
        @Volatile private var instance: RecoveryStore? = null
        fun get(context: Context): RecoveryStore = instance ?: synchronized(this) {
            instance ?: RecoveryStore(context.applicationContext).also { instance = it }
        }
    }
}
