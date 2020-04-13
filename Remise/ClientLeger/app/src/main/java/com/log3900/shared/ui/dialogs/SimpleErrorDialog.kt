package com.log3900.shared.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.log3900.login.LoginActivity

class SimpleErrorDialog : DialogFragment {
    private var dialog: AlertDialog

    constructor(context: Context, title: String, message: String, positiveButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                negativeButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?) : super() {

        dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok", positiveButtonListener)
            .create()
    }

    fun show() {
        dialog.show()
    }

}