package com.veil.app.services

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ephemeral health only. No frames, text, or confidence history is retained here. */
object MonitoringRuntime {
    data class ForegroundObservation(val packageName: String?, val revision: Long)
    private val mutableStatus = MutableStateFlow("Monitoring off. Screen sharing requires your consent.")
    val status: StateFlow<String> = mutableStatus.asStateFlow()
    private val mutableForegroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = mutableForegroundPackage.asStateFlow()
    private var foregroundRevision = 0L
    private val mutableRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = mutableRunning.asStateFlow()

    @Synchronized
    fun updateForegroundPackage(packageName: String?) {
        if (mutableForegroundPackage.value != packageName) foregroundRevision++
        mutableForegroundPackage.value = packageName
    }

    @Synchronized
    internal fun foregroundObservation() = ForegroundObservation(mutableForegroundPackage.value, foregroundRevision)

    internal fun updateRunning(running: Boolean) { mutableRunning.value = running }

    /** Immediately closes the policy gate before asynchronous native/service cleanup. */
    fun stop(context: Context) {
        mutableRunning.value = false
        mutableStatus.value = "Guardian stopped by you. New screen-sharing consent is required."
        context.stopService(Intent(context, VeilMonitoringService::class.java))
    }

    internal fun updateStatus(message: String) {
        mutableStatus.value = message
    }
}
