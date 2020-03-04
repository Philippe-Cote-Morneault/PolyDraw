package com.log3900.game.lobby

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.game.group.Difficulty
import com.log3900.game.group.Group
import com.log3900.game.group.GroupCreated
import com.log3900.game.group.MatchMode

class MatchCreationDialogFragment(var listener: Listener? = null) : DialogFragment() {
    private lateinit var groupNameTextInput: TextInputEditText
    private lateinit var maxPlayersTextInput: EditText
    private lateinit var virtualPlayersTextInput: EditText
    private lateinit var gameTypeSpinner: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle(resources.getString(R.string.create_match_dialog_title))
            .setPositiveButton("Create") { _, _ ->
                    listener?.onPositiveClick(GroupCreated(
                        groupNameTextInput.text.toString(),
                        maxPlayersTextInput.text.toString().toInt(),
                        virtualPlayersTextInput.text.toString().toInt(),
                        gameTypeSpinner.selectedItem as Int,
                        0))
            }
            .setNegativeButton("Cancel") { _, _ ->
                listener?.onNegativeClick()
            }

        val view = activity?.layoutInflater?.inflate(R.layout.dialog_fragment_create_match, null)!!

        setupView(view)

        dialogBuilder.setView(view)
        return dialogBuilder.create()
    }

    private fun setupView(rootView: View) {
        groupNameTextInput = rootView.findViewById(R.id.dialog_create_match_edit_text_match_name)
        maxPlayersTextInput = rootView.findViewById(R.id.dialog_create_match_edit_text_max_players)
        virtualPlayersTextInput = rootView.findViewById(R.id.dialog_create_match_edit_text_virtual_players)
        gameTypeSpinner = rootView.findViewById(R.id.dialog_create_match_spinner_match_type)

        setupSpinner()
    }

    private fun setupSpinner() {
        val matchModeSpinnerItems = ArrayList<String>()
        MatchMode.values().forEach {
            matchModeSpinnerItems.add(resources.getString(MatchMode.stringRes(it)))
        }

        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, matchModeSpinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gameTypeSpinner.adapter = adapter
    }


    interface Listener {
        fun onPositiveClick(groupCreated: GroupCreated) {}
        fun onNegativeClick() {}
    }
}