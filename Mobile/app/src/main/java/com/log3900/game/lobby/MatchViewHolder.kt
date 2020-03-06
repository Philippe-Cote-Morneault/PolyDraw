package com.log3900.game.lobby

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.game.group.MatchMode

class MatchViewHolder : RecyclerView.ViewHolder {
    private lateinit var match: Group

    // UI
    private var matchIconImageView: ImageView
    private var matchModeTextView: TextView
    private var matchNameTextView: TextView
    private var matchHostTextView: TextView
    private var matchPlayerCountTextView: TextView
    private var joinMatchButton: MaterialButton

    constructor(itemView: View, listener: Listener? = null) : super(itemView) {
        matchIconImageView = itemView.findViewById(R.id.list_item_match_image_view_match_type_icon)
        matchModeTextView = itemView.findViewById(R.id.list_item_match_text_view_match_mode)
        matchNameTextView = itemView.findViewById(R.id.list_item_match_text_view_match_name)
        matchHostTextView = itemView.findViewById(R.id.list_item_match_text_view_match_host)
        matchPlayerCountTextView = itemView.findViewById(R.id.list_item_match_text_view_match_player_count)
        joinMatchButton = itemView.findViewById(R.id.list_item_match_button_join)
        joinMatchButton.setOnClickListener {
            listener?.joinButtonClicked(match)
        }
    }

    fun bind(match: Group) {
        this.match = match
        matchModeTextView.setText(MatchMode.stringRes(match.gameType))
        matchNameTextView.text = match.groupName
        matchHostTextView.text = match.ownerName
        matchPlayerCountTextView.text = "${match.players.size}/${match.playersMax}"
        matchIconImageView.setImageResource(MatchMode.imageRes(match.gameType))
    }

    interface Listener {
        fun joinButtonClicked(group: Group)
    }
}