package com.log3900.game.lobby

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.game.group.*
import com.log3900.settings.language.LanguageManager
import com.log3900.user.account.AccountRepository

class MatchCreationDialogFragment(var listener: Listener? = null) : DialogFragment() {
    // UI
    private lateinit var groupNameTextInput: TextInputEditText
    private lateinit var maxPlayersTextView: TextView
    private lateinit var virtualPlayersTextView: TextView
    private lateinit var gameTypeSpinner: Spinner
    private lateinit var difficultySpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private lateinit var removeMaxPlayersButton: ImageView
    private lateinit var addMaxPlayersButton: ImageView
    private lateinit var removeVirtualPlayersButton: ImageView
    private lateinit var addVirtualPlayersButton: ImageView

    // Logic
    private var maxPlayersCurrentValue = 4
    private var virtualPlayersCurrentValye = 0
    private var currentMatchMode = MatchMode.FFA
    private var currentDifficulty = Difficulty.EASY
    private var currentLanguage: Language? = null
    private var availableLanguages: ArrayList<Language> = arrayListOf()

    private var addMaxPlayersButtonEnable = true
    private var removeMaxPlayersButtonEnable = true
    private var addVirtualPlayersButtonEnable = true
    private var removeVirtualPlayersButtonEnable = true


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle(resources.getString(R.string.create_match_dialog_title))
            .setPositiveButton("Create") { _, _ ->
                    listener?.onPositiveClick(GroupCreated(
                        groupNameTextInput.text.toString(),
                        maxPlayersTextView.text.toString().toInt(),
                        virtualPlayersTextView.text.toString().toInt(),
                        MatchMode.values()[gameTypeSpinner.selectedItemPosition],
                        Difficulty.values()[difficultySpinner.selectedItemPosition],
                        availableLanguages[languageSpinner.selectedItemPosition]))
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
        maxPlayersTextView = rootView.findViewById(R.id.dialog_create_match_text_view_max_players)
        virtualPlayersTextView = rootView.findViewById(R.id.dialog_create_match_text_view_virtual_players)
        gameTypeSpinner = rootView.findViewById(R.id.dialog_create_match_spinner_match_type)
        difficultySpinner = rootView.findViewById(R.id.dialog_create_match_spinner_difficulty)
        languageSpinner = rootView.findViewById(R.id.dialog_create_match_spinner_language)
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

        matchModeChange(MatchMode.values()[0])

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

        val languageSpinnerItems = ArrayList<String>()
        if (resources.configuration.locale.language == "en") {
            availableLanguages = arrayListOf(Language.ENGLISH, Language.FRENCH)
            currentLanguage = Language.ENGLISH
            languageSpinnerItems.add(resources.getString(Language.stringRes(Language.ENGLISH)))
            languageSpinnerItems.add(resources.getString(Language.stringRes(Language.FRENCH)))
        } else {
            availableLanguages = arrayListOf(Language.FRENCH, Language.ENGLISH)
            currentLanguage = Language.FRENCH
            languageSpinnerItems.add(resources.getString(Language.stringRes(Language.FRENCH)))
            languageSpinnerItems.add(resources.getString(Language.stringRes(Language.ENGLISH)))
        }



        val matchTypeAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, matchModeSpinnerItems)
        matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gameTypeSpinner.adapter = matchTypeAdapter
        gameTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                matchModeChange(MatchMode.values()[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val difficultyAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, difficultySpinnerItems)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        difficultySpinner.adapter = difficultyAdapter
        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                difficultyChange(Difficulty.values()[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val languageAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, languageSpinnerItems)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    private fun onRemoveMaxPlayersClick() {
        var newCount = maxPlayersTextView.text.toString().toInt() - 1
        maxPlayersChange(newCount)
    }

    private fun onAddMaxPlayersClick() {
        val newCount = maxPlayersTextView.text.toString().toInt() + 1
        maxPlayersChange(newCount)
    }

    private fun onRemoveVirtualPlayersClick() {
        val newCount = virtualPlayersTextView.text.toString().toInt() - 1
        virtualPlayersChange(newCount)
    }

    private fun onAddVirtualPlayersClick() {
        val newCount = virtualPlayersTextView.text.toString().toInt() + 1
        virtualPlayersChange(newCount)
    }

    private fun maxPlayersChange(newValue: Int) {
        if (newValue > Group.maxAmountOfPlayers(currentMatchMode) || newValue < Group.minAmountOfPlayers(currentMatchMode)) {
            return
        }

        if (newValue == Group.maxAmountOfPlayers(currentMatchMode)) {
            enableAddMaxPlayersButton(false)
        } else {
            enableAddMaxPlayersButton(true)
        }

        if (newValue == Group.minAmountOfPlayers(currentMatchMode)) {
            enableRemoveMaxPlayersButton(false)
        } else {
            enableRemoveMaxPlayersButton(true)
        }

        maxPlayersTextView.setText(newValue.toString())
        maxPlayersCurrentValue = newValue
    }

    private fun virtualPlayersChange(newValue: Int) {
        if (currentMatchMode == MatchMode.COOP || currentMatchMode == MatchMode.SOLO) {
            enableAddVirtualPlayersButton(false)
            enableRemoveVirtualPlayersButton(false)
            virtualPlayersTextView.setText(0.toString())
            return
        }

        if (newValue > maxPlayersCurrentValue - 1) {
            return
        }

        if (newValue == maxPlayersCurrentValue - 1) {
            enableAddVirtualPlayersButton(false)
        } else {
            enableAddVirtualPlayersButton(true)
        }

        if (newValue == 0) {
            enableRemoveVirtualPlayersButton(false)
        } else {
            enableRemoveVirtualPlayersButton(true)
        }

        virtualPlayersTextView.setText(newValue.toString())
        virtualPlayersCurrentValye = newValue
    }

    private fun matchModeChange(newValue: MatchMode) {
        currentMatchMode = newValue
        when (newValue) {
            MatchMode.SOLO -> {
                maxPlayersChange(Group.maxAmountOfPlayers(currentMatchMode))
                virtualPlayersChange(0)
            }
            MatchMode.FFA -> {
                maxPlayersChange(Group.maxAmountOfPlayers(currentMatchMode))
                virtualPlayersChange(0)
            }
            MatchMode.COOP -> {
                maxPlayersChange(Group.maxAmountOfPlayers(currentMatchMode))
                virtualPlayersChange(0)
            }
        }
    }

    private fun difficultyChange(newValue: Difficulty) {
        currentDifficulty = newValue
    }

    private fun enableAddMaxPlayersButton(enable: Boolean) {
        if (enable) {
            addMaxPlayersButtonEnable = true
            addMaxPlayersButton.colorFilter = null
        } else {
            addMaxPlayersButtonEnable = false
            addMaxPlayersButton.setColorFilter(Color.argb(255, 255, 255, 255))
        }
    }

    private fun enableRemoveMaxPlayersButton(enable: Boolean) {
        if (enable) {
            removeMaxPlayersButtonEnable = true
            removeMaxPlayersButton.colorFilter = null
        } else {
            removeMaxPlayersButtonEnable = false
            removeMaxPlayersButton.setColorFilter(Color.argb(255, 255, 255, 255))
        }
    }

    private fun enableAddVirtualPlayersButton(enable: Boolean) {
        if (enable) {
            addVirtualPlayersButtonEnable = true
            addVirtualPlayersButton.colorFilter = null
        } else {
            addVirtualPlayersButtonEnable = false
            addVirtualPlayersButton.setColorFilter(Color.argb(255, 255, 255, 255))
        }
    }

    private fun enableRemoveVirtualPlayersButton(enable: Boolean) {
        if (enable) {
            removeVirtualPlayersButtonEnable = true
            removeVirtualPlayersButton.colorFilter = null
        } else {
            removeVirtualPlayersButtonEnable = false
            removeVirtualPlayersButton.setColorFilter(Color.argb(255, 255, 255, 255))
        }
    }


    interface Listener {
        fun onPositiveClick(groupCreated: GroupCreated) {}
        fun onNegativeClick() {}
    }
}