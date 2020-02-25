package com.log3900.shared.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.log3900.MainApplication

class SimpleConfirmationDialog : DialogFragment {
    private var dialog: AlertDialog

    constructor(title: String, message: String, positiveButtonListener: ((dialog: DialogInterface, which: Int) -> Unit),
                negativeButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)) : super() {

        dialog = AlertDialog.Builder(MainApplication.instance.applicationContext)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes", positiveButtonListener)
            .setNegativeButton("No", negativeButtonListener)
            .create()
    }
}