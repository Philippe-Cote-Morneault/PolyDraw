package com.log3900.chat.Channel

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.R

class ChannelViewHolder : RecyclerView.ViewHolder {
    private var channelName: TextView
    var buttonAction1: MaterialButton
    var buttonAction2: MaterialButton

    constructor(itemView: View) : super(itemView){
        channelName = itemView.findViewById(R.id.list_item_channel_name)
        buttonAction1 = itemView.findViewById(R.id.list_item_channel_button_action_1)
        buttonAction2 = itemView.findViewById(R.id.list_item_channel_button_action_2)
    }

    fun bind(channel: Channel) {
        channelName.setText(channel.name)
    }
}
