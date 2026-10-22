package com.veil.app.core

/** Aggregate technical counters only. No package names, scores, frames, or wall-clock activity. */
class ProtectionTelemetry {
    enum class Event { SERVICE_CREATED, START_REQUESTED, CAPTURE_STARTED, TASK_REMOVED, CAPTURE_STOPPED, SERVICE_DESTROYED, MODEL_READY, MODEL_FAILED, INFERENCE_FAILED, FRAME_STALE, ACCESS_REVOKED }
    data class Snapshot(val events: Map<Event, Long>, val inferences: Long, val totalInferenceNanos: Long, val maxInferenceNanos: Long, val policyTriggers: Long) {
        val meanInferenceMillis: Double get() = if (inferences == 0L) 0.0 else totalInferenceNanos / inferences / 1_000_000.0
    }
    private val events = mutableMapOf<Event, Long>()
    private var inferences = 0L
    private var totalNanos = 0L
    private var maxNanos = 0L
    private var policyTriggers = 0L
    @Synchronized fun event(event: Event) { events[event] = (events[event] ?: 0) + 1 }
    @Synchronized fun inference(nanos: Long) {
        require(nanos >= 0)
        inferences++; totalNanos += nanos; maxNanos = maxOf(maxNanos, nanos)
    }
    @Synchronized fun policyTrigger() { policyTriggers++ }
    @Synchronized fun snapshot() = Snapshot(events.toMap(), inferences, totalNanos, maxNanos, policyTriggers)
    @Synchronized fun clear() { events.clear(); inferences = 0; totalNanos = 0; maxNanos = 0; policyTriggers = 0 }
}
