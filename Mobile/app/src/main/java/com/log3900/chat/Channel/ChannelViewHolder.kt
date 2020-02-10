package com.log3900.chat.Channel

import android.view.View
import android.widget.TextView
import com.log3900.R
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder

class ChannelViewHolder : ChildViewHolder {
    private var channelName: TextView
    constructor(itemView: View) : super(itemView){
        channelName = itemView.findViewById(R.id.list_item_channel_name)
    }

    fun bind(channel: Channel) {
        channelName.setText(channel.name)
    }
}
