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
import com.log3900.utils.format.DateFormatter
import java.util.*

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
        if (match.matchType == "FFA") {
            result.text = resources.getString(
                if (username == match.winnerName)
                    R.string.match_win
                else
                    R.string.match_loss
            )
        } else {
            result.text = "-"
            root.findViewById<TextView>(R.id.match_winner_title).setText(R.string.points_title)
        }

        val winner: TextView = root.findViewById(R.id.match_winner_value)
        winner.text = if (match.winnerName.isEmpty()) "-" else match.winnerName

        val duration: TextView = root.findViewById(R.id.match_duration_value)
        val durationDate = Date(match.duration)
        duration.text = DateFormatter.formatFullTime(durationDate)

        val playersLayout: LinearLayout = root.findViewById(R.id.match_players_container)
        match.players.forEach {
            playersLayout.addPlayer(it)
        }

        val closeButton: MaterialButton = root.findViewById(R.id.close_dialog_button)
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun LinearLayout.addPlayer(player: Player) {
        val nameView = TextView(activity)
        nameView.text = player.name
        this.addView(nameView)
    }

    private fun setDialogLayout() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}