package com.log3900.chat.Channel

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class ChannelGroupViewHolder : RecyclerView.ViewHolder {
    var expandableIcon: ImageView
    private var channelGroupNameTextView: TextView

    lateinit var channelGroup: ChannelGroup

    constructor(itemView: View) : super(itemView) {
        expandableIcon = itemView.findViewById(R.id.list_item_channel_group_icon_expandable)
        channelGroupNameTextView = itemView.findViewById(R.id.list_item_channel_group_name)
    }

    fun bind(channelGroup: ChannelGroup) {
        channelGroupNameTextView.setText(channelGroup.name)
        this.channelGroup = channelGroup
    }
}

