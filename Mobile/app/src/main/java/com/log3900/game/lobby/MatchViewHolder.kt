package com.log3900.game.lobby

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Group

class MatchViewHolder : RecyclerView.ViewHolder {
    private lateinit var match: Group

    // UI
    private var matchIconImageView: ImageView
    private var matchModeTextView: TextView
    private var matchNameTextView: TextView
    private var matchHostTextView: TextView
    private var matchPlayerCountTextView: TextView

    constructor(itemView: View) : super(itemView) {
        matchIconImageView = itemView.findViewById(R.id.list_item_match_image_view_match_type_icon)
        matchModeTextView = itemView.findViewById(R.id.list_item_match_text_view_match_mode)
        matchNameTextView = itemView.findViewById(R.id.list_item_match_text_view_match_name)
        matchHostTextView = itemView.findViewById(R.id.list_item_match_text_view_match_host)
        matchPlayerCountTextView = itemView.findViewById(R.id.list_item_match_text_view_match_player_count)
    }

    fun bind(match: Group) {
        this.match = match
        matchModeTextView.text = match.gameType.toString()
        matchNameTextView.text = match.groupName
        matchHostTextView.text = match.owner.toString()
        matchPlayerCountTextView.text = "${match.players.size}/${match.playersMax}"
    }
}