package com.log3900.utils.ui

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

class KeyboardHelper {
    companion object {

        /**
         * Hides the Android keyboard if it is active
         *
         * @param activity the current activity in which the keyboard is shown
         */
        fun hideKeyboard(activity: Activity) {
            val inputManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus

            if (view == null) {
                view = View(activity)
            }

            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}