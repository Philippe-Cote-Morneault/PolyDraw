package com.log3900.shared.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment

class SimpleConfirmationDialog : DialogFragment {
    private var dialog: AlertDialog

    constructor(context: Context, title: String, message: String, positiveButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                negativeButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?) : super() {

        dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes", positiveButtonListener)
            .setNegativeButton("No", negativeButtonListener)
            .create()
    }

    fun show() {
        dialog.show()
    }

}