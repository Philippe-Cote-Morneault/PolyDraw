package com.log3900.settings.theme

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.log3900.MainApplication
import com.log3900.R

class ThemeManager {
    companion object {
        private val defaultThemeID = R.style.MyTheme_IndigoOrange
        private val registeredActivities: HashMap<Activity, Int> = HashMap()
        var themes: HashMap<Int, Theme> = hashMapOf(
            R.style.MyTheme_BlueYellow to Theme(R.style.MyTheme_BlueYellow, R.drawable.blue_yellow_theme_button),
            R.style.MyTheme_BrownGreen to Theme(R.style.MyTheme_BrownGreen, R.drawable.brown_green_theme_button),
            R.style.MyTheme_GreenTeal to Theme(R.style.MyTheme_GreenTeal, R.drawable.green_teal_theme_button),
            R.style.MyTheme_IndigoOrange to Theme(R.style.MyTheme_IndigoOrange, R.drawable.indigo_orange_theme_button),
            R.style.MyTheme_OrangeLightBlue to Theme(R.style.MyTheme_OrangeLightBlue, R.drawable.orange_light_blue_theme_button),
            R.style.MyTheme_PurpleGreen to Theme(R.style.MyTheme_PurpleGreen, R.drawable.purple_green_theme_button),
            R.style.MyTheme_RedYellow to Theme(R.style.MyTheme_RedYellow, R.drawable.red_yellow_theme_button),
            R.style.MyTheme_TealLime to Theme(R.style.MyTheme_TealLime, R.drawable.teal_lime_theme_button)
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