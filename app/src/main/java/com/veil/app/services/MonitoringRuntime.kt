package com.veil.app.services

import android.content.Context
import android.content.Intent
import android.os.Build
import com.veil.app.core.ProtectionTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Runtime health is deliberately ephemeral: a new process never claims old capture consent. */
object MonitoringRuntime {
    enum class CaptureState { OFF, STARTING, WAITING, ANALYZING, PAUSED, NO_RECENT_FRAMES, INTERRUPTED }
    data class Health(val serviceRunning: Boolean = false, val captureState: CaptureState = CaptureState.OFF, val modelReady: Boolean = false, val inferenceReady: Boolean = false, val resumeRequired: Boolean = true)
    data class ForegroundObservation(val packageName: String?, val revision: Long)
    private val mutableHealth = MutableStateFlow(Health())
    val health: StateFlow<Health> = mutableHealth.asStateFlow()
    val telemetry = ProtectionTelemetry()
    private val mutableStatus = MutableStateFlow("Monitoring off. Screen sharing requires your consent.")
    val status: StateFlow<String> = mutableStatus.asStateFlow()
    private val mutableForegroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = mutableForegroundPackage.asStateFlow()
    private var foregroundRevision = 0L
    private val mutableRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = mutableRunning.asStateFlow()

    @Synchronized fun updateForegroundPackage(packageName: String?) {
        if (mutableForegroundPackage.value != packageName) foregroundRevision++
        mutableForegroundPackage.value = packageName
    }
    @Synchronized internal fun foregroundObservation() = ForegroundObservation(mutableForegroundPackage.value, foregroundRevision)
    @Synchronized internal fun updateRunning(running: Boolean) {
        mutableRunning.value = running
        mutableHealth.value = mutableHealth.value.copy(serviceRunning = running,
            captureState = if (running) CaptureState.STARTING else CaptureState.OFF,
            resumeRequired = true, inferenceReady = false)
    }
    @Synchronized internal fun modelReady(ready: Boolean) {
        mutableHealth.value = mutableHealth.value.copy(modelReady = ready, inferenceReady = ready && mutableRunning.value)
    }
    @Synchronized internal fun captureState(state: CaptureState) {
        mutableHealth.value = mutableHealth.value.copy(captureState = state,
            resumeRequired = state in setOf(CaptureState.OFF, CaptureState.STARTING, CaptureState.INTERRUPTED))
    }
    /** Immediately closes policy gate before asynchronous native/service cleanup. */
    fun stop(context: Context) {
        updateRunning(false)
        mutableStatus.value = "Guardian stopped by you. New screen-sharing consent is required."
        context.stopService(Intent(context, VeilMonitoringService::class.java))
    }
    internal fun updateStatus(message: String) { mutableStatus.value = message }

    /** User-initiated export. Fixed fields only; never screen activity, journals, preferences or scores. */
    fun diagnosticText(context: Context): String {
        val current = health.value
        val counters = telemetry.snapshot()
        @Suppress("DEPRECATION")
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        return buildString {
            appendLine("Veil technical diagnostics schema=1")
            appendLine("appVersion=$version androidApi=${Build.VERSION.SDK_INT}")
            appendLine("model=${VeilModelSpec.version}")
            appendLine("service=${current.serviceRunning} capture=${current.captureState} modelReady=${current.modelReady} inferenceReady=${current.inferenceReady} resumeRequired=${current.resumeRequired}")
            appendLine("foregroundAwareness=${CommitmentStore.get(context).accessibilityConnected.value}")
            appendLine("focusActive=${CommitmentStore.get(context).state.value.focusRemaining(android.os.SystemClock.elapsedRealtime(), CommitmentStore.get(context).bootCount()) > 0}")
            appendLine("inferences=${counters.inferences} totalInferenceNanos=${counters.totalInferenceNanos} maxInferenceNanos=${counters.maxInferenceNanos} policyTriggers=${counters.policyTriggers}")
            counters.events.toSortedMap(compareBy { it.name }).forEach { (event, count) -> appendLine("${event.name}=$count") }
            appendLine("Counters cover this app process only. Timing covers the classifier call including model preprocessing; excludes capture and overlay. No semantic accuracy is implied.")
        }
    }
}
