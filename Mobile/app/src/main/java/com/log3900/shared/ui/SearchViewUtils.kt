package com.log3900.shared.ui

import android.view.View
import android.view.ViewGroup


object SearchViewUtils {
    fun isFocused(view: View): Boolean {
        if (view.isFocused())
            return true

        if (view is ViewGroup) {
            val viewGroup = view
            for (i in 0 until viewGroup.childCount) {
                if (isFocused(viewGroup.getChildAt(i)))
                    return true
            }
        }
        return false
    }
}