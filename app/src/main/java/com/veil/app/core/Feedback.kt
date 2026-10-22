package com.veil.app.core

enum class Correction { EXPLICIT, NOT_EXPLICIT, UNSURE, MISSED }
data class FeedbackEntry(val id: String, val at: Long, val correction: Correction, val model: String)

/** Feedback is a report, never a policy override or a visual training sample. */
object FeedbackPolicy {
    const val retentionMs = 30L * 24 * 60 * 60 * 1000
    fun retained(entries: List<FeedbackEntry>, now: Long): List<FeedbackEntry> =
        entries.filter { it.at in (now - retentionMs)..now }.distinctBy { it.id }.takeLast(100)
}
