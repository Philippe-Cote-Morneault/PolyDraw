package com.log3900.shared.ui

import android.content.res.Resources
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.settings.theme.ThemeManager

object ThemeUtils {
    fun resolveAttribute(attribute: Int): Int {
        var value = TypedValue()
        ContextThemeWrapper(MainApplication.instance.baseContext, ThemeManager.getCurrentTheme().themeID)
            .theme.resolveAttribute(attribute, value, true)
        return value.data
    }
}