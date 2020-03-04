package com.log3900.game.lobby

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.log3900.R

class MatchCreationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle(Resources.getSystem().getString(R.string.create_match_dialog_title))
            .setPositiveButton("Create") { _, _ ->

            }
            .setNegativeButton("Cancel") { _, _ ->

            }

        val view = activity?.layoutInflater?.inflate(R.layout.dialog_fragment_create_match, null)

        dialogBuilder.setView(view)
        return dialogBuilder.create()
    }
}