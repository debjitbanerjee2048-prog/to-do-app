package com.neontasks.hud.data

/** Rule-based auto-priority: deadline proximity first, then keywords. */
object PriorityEngine {
    private val criticalWords = Regex(
        "\\b(urgent|asap|emergency|critical|outage|deadline|overdue|immediately|fix|bug|now)\\b"
    )
    private val highWords = Regex(
        "\\b(meeting|submit|pay|bill|invoice|call|review|exam|report|client|interview|deliver)\\b"
    )

    fun classify(
        title: String,
        notes: String = "",
        deadline: Long?,
        now: Long = System.currentTimeMillis()
    ): Priority {
        val text = "$title $notes".lowercase()
        val hoursLeft = deadline?.let { (it - now) / 3_600_000.0 }
        return when {
            hoursLeft != null && hoursLeft <= 6 -> Priority.CRITICAL
            criticalWords.containsMatchIn(text) -> Priority.CRITICAL
            hoursLeft != null && hoursLeft <= 48 -> Priority.HIGH
            highWords.containsMatchIn(text) -> Priority.HIGH
            else -> Priority.ROUTINE
        }
    }
}
