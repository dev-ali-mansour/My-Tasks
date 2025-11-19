package dev.alimansour.mytasks.core.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper


val Context.activity: Activity
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        error("No activity found in context hierarchy!")
    }
