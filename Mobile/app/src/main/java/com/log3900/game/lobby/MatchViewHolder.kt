package com.log3900.game.lobby

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.group.Difficulty
import com.log3900.game.group.Group
import com.log3900.game.group.Language
import com.log3900.game.group.MatchMode
import com.log3900.utils.format.DateFormatter
import java.util.*

class MatchViewHolder : RecyclerView.ViewHolder {
    private lateinit var match: Group

    // UI
    private var matchIconImageView: ImageView
    private var matchModeTextView: TextView
    private var matchLanguageTextView: TextView
    private var matchDifficultyTextView: TextView
    private var matchHostTextView: TextView
    private var matchDurationTextView: TextView
    private var matchPlayerCountTextView: TextView
    private var joinMatchButton: MaterialButton

    constructor(itemView: View, listener: Listener? = null) : super(itemView) {
        matchIconImageView = itemView.findViewById(R.id.list_item_match_image_view_match_type_icon)
        matchModeTextView = itemView.findViewById(R.id.list_item_match_text_view_match_mode)
        matchLanguageTextView = itemView.findViewById(R.id.list_item_match_text_view_match_language)
        matchDifficultyTextView = itemView.findViewById(R.id.list_item_match_text_view_match_difficulty)
        matchHostTextView = itemView.findViewById(R.id.list_item_match_text_view_match_host)
        matchDurationTextView = itemView.findViewById(R.id.list_item_match_text_view_match_duration)
        matchPlayerCountTextView = itemView.findViewById(R.id.list_item_match_text_view_match_player_count)
        joinMatchButton = itemView.findViewById(R.id.list_item_match_button_join)
        joinMatchButton.setOnClickListener {
            listener?.joinButtonClicked(match)
        }
        itemView.setOnClickListener {
            listener?.joinButtonClicked(match)
        }
    }

    fun bind(match: Group) {
        this.match = match
        matchModeTextView.setText(MatchMode.stringRes(match.gameType))
        matchLanguageTextView.text = MainApplication.instance.getContext().resources.getString(Language.stringRes(match.language))
        matchDifficultyTextView.text = MainApplication.instance.getContext().resources.getString(Difficulty.stringRes(match.difficulty))
        matchHostTextView.text = match.ownerName
        if (match.gameType == MatchMode.FFA) {
            matchDurationTextView.text = DateFormatter.formatDateToTime(Date((match.players.size * match.rounds!! * 1000 * 60).toLong()))
        } else {
            matchDurationTextView.text = DateFormatter.formatDateToTime(Date(Difficulty.timeToPlay(match.difficulty)))
        }
        matchPlayerCountTextView.text = "${match.players.size}/${match.playersMax}"
        matchIconImageView.setImageResource(MatchMode.imageRes(match.gameType))
    }

    interface Listener {
        fun joinButtonClicked(group: Group)
    }
}