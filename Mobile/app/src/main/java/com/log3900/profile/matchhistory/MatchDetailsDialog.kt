package com.log3900.profile.matchhistory

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.profile.stats.GamePlayed
import com.log3900.profile.stats.Player
import com.log3900.utils.format.formatDuration

class MatchDetailsDialog(private val match: GamePlayed, private val username: String)
    : DialogFragment() {

    override fun onStart() {
        super.onStart()
        setDialogLayout()
    }

    override fun onResume() {
        super.onResume()
        setDialogLayout()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_match_played, container, false)
        setUpUi(rootView)
        return rootView
    }

    private fun setUpUi(root: View) {
        val type: TextView = root.findViewById(R.id.match_type_value)
        type.text = match.matchType

        val result: TextView = root.findViewById(R.id.match_result_value)
        result.text = resources.getString(
            if (username == match.winnerName)
                R.string.match_win
            else
                R.string.match_loss
        )

        val winner: TextView = root.findViewById(R.id.match_winner_value)
        winner.text = match.winnerName

        val duration: TextView = root.findViewById(R.id.match_duration_value)
        duration.text = formatDuration(match.duration)

//        val playersLayout: LinearLayout = root.findViewById(R.id.match_players_container)
//        match.playerNames.forEach {
//            playersLayout.addPlayer(it)
//        }

        val closeButton: MaterialButton = root.findViewById(R.id.close_dialog_button)
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun LinearLayout.addPlayer(playerName: Player) {
        val nameView = TextView(activity)
        nameView.text = playerName.name
        this.addView(nameView)
    }

    private fun setDialogLayout() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}