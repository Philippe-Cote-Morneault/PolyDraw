package com.log3900.settings

import android.app.Activity
import com.log3900.R

class ThemeManager {
    companion object {
        fun setTheme(activity: Activity) {
            activity.setTheme(R.style.MyTheme_BlueYellow)
        }
    }
}