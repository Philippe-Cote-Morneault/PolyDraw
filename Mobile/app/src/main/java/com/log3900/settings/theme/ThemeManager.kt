package com.log3900.settings.theme

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.user.account.AccountRepository
import io.reactivex.Completable

class ThemeManager {
    companion object {
        private val registeredActivities: HashMap<Activity, Int> = HashMap()
        var themes: ArrayList<Theme> = arrayListOf(
            Theme(0, R.style.MyTheme_BlueYellow, R.drawable.blue_yellow_theme_button),
            Theme(1, R.style.MyTheme_BrownGreen, R.drawable.brown_green_theme_button),
            Theme(2, R.style.MyTheme_GreenTeal, R.drawable.green_teal_theme_button),
            Theme(3, R.style.MyTheme_IndigoOrange, R.drawable.indigo_orange_theme_button),
            Theme(4, R.style.MyTheme_OrangeLightBlue, R.drawable.orange_light_blue_theme_button),
            Theme(5, R.style.MyTheme_PurpleGreen, R.drawable.purple_green_theme_button),
            Theme(6, R.style.MyTheme_RedYellow, R.drawable.red_yellow_theme_button),
            Theme(7, R.style.MyTheme_TealLime, R.drawable.teal_lime_theme_button)
        )

        fun applyTheme(activity: Activity) {
            activity.setTheme(
                getCurrentTheme().themeID
            )

            registeredActivities[activity] = getCurrentTheme().index
        }

        fun hasActivityThemeChanged(activity: Activity): Boolean {
            return registeredActivities[activity] != getCurrentTheme().index
        }

        fun getCurrentTheme(): Theme {
            return themes[AccountRepository.getInstance().getAccount().themeID]
        }

        fun changeTheme(theme: Theme): Completable {
            Log.d("POTATO", "ThemeManager::changeTheme(${theme.index})")
            val currentAccount = AccountRepository.getInstance().getAccount()
            currentAccount.themeID = theme.index
            return AccountRepository.getInstance().updateAccount(currentAccount)
        }

        fun getThemesAsArrayList(): ArrayList<Theme> {
            return themes
        }
    }
}