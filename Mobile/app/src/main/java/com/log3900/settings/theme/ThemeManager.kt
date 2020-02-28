package com.log3900.settings

import android.app.Activity
import android.content.Context
import com.log3900.MainApplication
import com.log3900.R

class ThemeManager {
    companion object {
        private val themeResourceIDs = intArrayOf(R.style.MyTheme_BlueYellow, R.style.MyTheme_OrangeBlue)
        fun applyTheme(activity: Activity) {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val theme = preferences.getInt(context.getString(R.string.preference_file_theme_key), 0)
            activity.setTheme(getThemeResourceFromIndex(theme))
        }

        fun changeTheme() {

        }

        private fun getThemeResourceFromIndex(themeIndex: Int): Int {
            return themeResourceIDs[themeIndex]
        }
    }
}