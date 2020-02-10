package com.log3900.chat.Channel

import android.view.View
import android.widget.TextView
import com.log3900.R
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.list_item_channel_group.view.*

class ChannelGroupViewHolder : GroupViewHolder {
    private var channelGroupNameTextView: TextView
    constructor(itemView: View) : super(itemView) {
        channelGroupNameTextView = itemView.findViewById(R.id.list_item_channel_group_name)
    }

    fun onBind(channelGroup: ChannelGroup) {
        channelGroupNameTextView.setText(channelGroup.name)
    }
}
