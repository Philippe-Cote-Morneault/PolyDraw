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
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.game.group.Difficulty
import com.log3900.game.group.Group
import com.log3900.game.group.GroupCreated
import com.log3900.game.group.MatchMode

class MatchCreationDialogFragment(var listener: Listener? = null) : DialogFragment() {
    // UI
    private lateinit var groupNameTextInput: TextInputEditText
    private lateinit var maxPlayersTextInput: EditText
    private lateinit var virtualPlayersTextInput: EditText
    private lateinit var gameTypeSpinner: Spinner
    private lateinit var difficultySpinner: Spinner
    private lateinit var removeMaxPlayersButton: ImageView
    private lateinit var addMaxPlayersButton: ImageView
    private lateinit var removeVirtualPlayersButton: ImageView
    private lateinit var addVirtualPlayersButton: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle(resources.getString(R.string.create_match_dialog_title))
            .setPositiveButton("Create") { _, _ ->
                    listener?.onPositiveClick(GroupCreated(
                        groupNameTextInput.text.toString(),
                        maxPlayersTextInput.text.toString().toInt(),
                        virtualPlayersTextInput.text.toString().toInt(),
                        MatchMode.values()[gameTypeSpinner.selectedItemPosition],
                        Difficulty.values()[difficultySpinner.selectedItemPosition]))
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
        difficultySpinner = rootView.findViewById(R.id.dialog_create_match_spinner_difficulty)
        removeMaxPlayersButton = rootView.findViewById(R.id.dialog_create_match_button_remove_max_player)
        addMaxPlayersButton = rootView.findViewById(R.id.dialog_create_match_button_add_max_player)
        removeVirtualPlayersButton = rootView.findViewById(R.id.dialog_create_match_button_remove_virtual_player)
        addVirtualPlayersButton = rootView.findViewById(R.id.dialog_create_match_button_add_virtual_player)

        removeMaxPlayersButton.setOnClickListener {
            onRemoveMaxPlayersClick()
        }

        addMaxPlayersButton.setOnClickListener {
            onAddMaxPlayersClick()
        }

        removeVirtualPlayersButton.setOnClickListener {
            onRemoveVirtualPlayersClick()
        }

        addVirtualPlayersButton.setOnClickListener {
            onAddVirtualPlayersClick()
        }

        setupSpinners()
    }

    private fun setupSpinners() {
        val matchModeSpinnerItems = ArrayList<String>()
        MatchMode.values().forEach {
            matchModeSpinnerItems.add(resources.getString(MatchMode.stringRes(it)))
        }

        val difficultySpinnerItems = ArrayList<String>()
        Difficulty.values().forEach {
            difficultySpinnerItems.add(resources.getString(Difficulty.stringRes(it)))
        }

        val matchTypeAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, matchModeSpinnerItems)
        matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gameTypeSpinner.adapter = matchTypeAdapter

        val difficultyAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, difficultySpinnerItems)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        difficultySpinner.adapter = difficultyAdapter
    }

    private fun onRemoveMaxPlayersClick() {
        var newCount = maxPlayersTextInput.text.toString().toInt() - 1
        maxPlayersTextInput.setText(newCount.toString())
    }

    private fun onAddMaxPlayersClick() {
        val newCount = maxPlayersTextInput.text.toString().toInt() + 1
        maxPlayersTextInput.setText(newCount.toString())
    }

    private fun onRemoveVirtualPlayersClick() {
        val newCount = virtualPlayersTextInput.text.toString().toInt() - 1
        virtualPlayersTextInput.setText(newCount.toString())
    }

    private fun onAddVirtualPlayersClick() {
        val newCount = virtualPlayersTextInput.text.toString().toInt() + 1
        virtualPlayersTextInput.setText(newCount.toString())
    }


    interface Listener {
        fun onPositiveClick(groupCreated: GroupCreated) {}
        fun onNegativeClick() {}
    }
}