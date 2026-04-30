package dev.alimansour.mytasks.core.domain.util

import dev.alimansour.mytasks.core.domain.model.TaskPriority

class SmartPriorityAnalyzer {

    fun analyze(title: String): TaskPriority {
        val keywords = listOf("urgent", "asap", "tomorrow")
        val isHighPriority = keywords.any { keyword ->
            title.contains(keyword, ignoreCase = true)
        }
        return if (isHighPriority) TaskPriority.HIGH else TaskPriority.LOW
    }
}
