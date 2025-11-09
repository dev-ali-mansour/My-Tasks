package dev.alimansour.mytasks.core.ui.utils

import android.content.Context
import dev.alimansour.mytasks.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Context.formatDueDate(dueDateMillis: Long): String {
    val today = LocalDate.now()
    val dueDate = Instant.ofEpochMilli(dueDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()

    val daysBetween = ChronoUnit.DAYS.between(today, dueDate)

    return when {
        daysBetween < 0 -> {
            val daysAgo = -daysBetween
            resources.getQuantityString(R.plurals.due_days_ago, daysAgo.toInt(), daysAgo)
        }
        daysBetween == 0L -> getString(R.string.due_today)
        daysBetween == 1L -> getString(R.string.due_tomorrow)
        else -> getString(R.string.due_in_days, daysBetween)
    }
}
