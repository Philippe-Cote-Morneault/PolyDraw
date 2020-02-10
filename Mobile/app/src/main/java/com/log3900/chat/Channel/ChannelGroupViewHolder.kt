package com.log3900.chat.Channel

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class ChannelGroupViewHolder : RecyclerView.ViewHolder {
    private var channelGroupNameTextView: TextView
    constructor(itemView: View) : super(itemView) {
        channelGroupNameTextView = itemView.findViewById(R.id.list_item_channel_group_name)
    }

    fun bind(channelGroup: ChannelGroup) {
        channelGroupNameTextView.setText(channelGroup.name)
    }
}
