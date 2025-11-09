package dev.alimansour.mytasks.core.ui.utils

import android.content.Context
import androidx.annotation.StringRes

sealed interface UiText {
    data class DynamicString(
        val value: String,
    ) : UiText

    class StringResourceId(
        @param:StringRes val id: Int,
        val args: Array<Any> = arrayOf(),
    ) : UiText

    fun asString(context: Context): String =
        when (this) {
            is DynamicString -> value
            is StringResourceId -> context.getString(id, args)
        }
}
