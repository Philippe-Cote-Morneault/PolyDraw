package com.log3900.utils.format

import android.app.Activity
import android.text.format.DateUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {
    companion object {

        fun formatDate(date: Date): String {
            var dateFormat: SimpleDateFormat? = null
            if (DateUtils.isToday(date.time)) {
                dateFormat = SimpleDateFormat("HH:mm:ss")
            }
            else {
                dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            }
            val dateString = dateFormat.format(date)
            return dateString
        }

    }
}