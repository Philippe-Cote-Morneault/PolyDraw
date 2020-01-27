package com.log3900.utils.format

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {
    companion object {

        fun formatDate(date: Date): String {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            val dateString = dateFormat.format(date)
            return dateString
        }

    }
}