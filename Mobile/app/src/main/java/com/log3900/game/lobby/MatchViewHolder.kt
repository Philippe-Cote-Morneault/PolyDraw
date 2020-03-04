package com.log3900.game.lobby

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class MatchViewHolder : RecyclerView.ViewHolder {
    
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

    fun bind() {

    }
}