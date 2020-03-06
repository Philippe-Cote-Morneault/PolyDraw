package com.log3900.shared.ui

import android.util.TypedValue
import android.view.ContextThemeWrapper
import com.log3900.MainApplication
import com.log3900.settings.theme.ThemeManager

object ThemeUtils {
    fun resolveAttribute(attribute: Int): Int {
        var value = TypedValue()
        ContextThemeWrapper(MainApplication.instance.baseContext, ThemeManager.getCurrentTheme().themeID)
            .theme.resolveAttribute(attribute, value, true)
        return value.data
    }
}