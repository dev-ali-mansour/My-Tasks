package dev.alimansour.mytasks.core.ui.utils

import android.content.Context
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Long.getFormattedDate(
    context: Context,
    pattern: String = "dd/MM/yyyy",
): String = getFormattedDateTime(context, pattern)

fun Long.getFormattedDateTime(
    context: Context,
    pattern: String = "dd/MM/yyyy hh:mm a",
): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val configuration = context.resources.configuration
    val locale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val dateFormat = SimpleDateFormat(pattern, locale)
    return dateFormat.format(calendar.time)
}
