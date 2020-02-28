package com.log3900.settings.theme

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.log3900.MainApplication
import com.log3900.R

class ThemeManager {
    companion object {
        private val defaultThemeID = R.style.MyTheme_BlueYellow
        private val registeredActivities: HashMap<Activity, Int> = HashMap()
        var themes: HashMap<Int, Theme> = hashMapOf(
            R.style.MyTheme_BlueYellow to Theme(R.style.MyTheme_BlueYellow, R.drawable.green_turtoise_theme_button),
            R.style.MyTheme_OrangeBlue to Theme(R.style.MyTheme_OrangeBlue, R.drawable.blue_orang_theme_button)
        )

        fun applyTheme(activity: Activity) {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val theme = preferences.getInt(context.getString(R.string.preference_file_theme_key), defaultThemeID)
            activity.setTheme(
                themes[theme]?.themeID!!
            )

            registeredActivities[activity] = theme
        }

        fun hasActivityThemeChanged(activity: Activity): Boolean {
            return registeredActivities[activity] != getCurrentTheme().themeID
        }

        fun getCurrentTheme(): Theme {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val themeID = preferences.getInt(context.getString(R.string.preference_file_theme_key), defaultThemeID)
            return themes[themeID]!!
        }

        fun changeTheme(theme: Theme) {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            preferences.edit {
                putInt(context.getString(R.string.preference_file_theme_key), theme.themeID)
                commit()
            }
        }

        fun getThemesAsArrayList(): ArrayList<Theme> {
            val arrayList = arrayListOf<Theme>()

            themes.forEach {
                arrayList.add(it.value)
            }

            return arrayList
        }
    }
}