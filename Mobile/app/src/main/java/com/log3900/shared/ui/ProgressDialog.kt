package com.log3900.shared.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.log3900.R

class ProgressDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        /*
        dialog.setOnKeyListener(DialogInterface.OnKeyListener{ v, actionID, event ->
            if (actionID == android.view.KeyEvent.KEYCODE_BACK) {
                activity?.finish()
                true
            }
            else {
                false
            }
        })
*/
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_progress_dialog, container, false)


        return rootView
    }

}