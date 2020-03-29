package com.log3900.shared.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.log3900.R

class SimpleConfirmationDialog : DialogFragment {
    private var dialog: AlertDialog

    constructor(context: Context, title: String, message: String, positiveButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                negativeButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?) : super() {

        dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.yes, positiveButtonListener)
            .setNegativeButton(R.string.no, negativeButtonListener)
            .create()
    }

    fun show() {
        dialog.show()
    }

}