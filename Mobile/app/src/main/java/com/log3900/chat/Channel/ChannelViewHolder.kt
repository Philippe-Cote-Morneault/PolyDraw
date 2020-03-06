package com.log3900.chat.Channel

import android.graphics.Color
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.shared.ui.ThemeUtils

class ChannelViewHolder : RecyclerView.ViewHolder {
    private var rootView: ConstraintLayout
    private var unreadMessagesCounter: TextView
    private var channelName: TextView
    var buttonAction1: ImageButton
    var buttonAction2: ImageButton

    constructor(itemView: View) : super(itemView){
        rootView = itemView.findViewById(R.id.list_item_channel_root_view)
        unreadMessagesCounter = itemView.findViewById(R.id.list_item_channel_unread_counter)
        channelName = itemView.findViewById(R.id.list_item_channel_name)
        buttonAction1 = itemView.findViewById(R.id.list_item_channel_button_action_1)
        buttonAction2 = itemView.findViewById(R.id.list_item_channel_button_action_2)
    }

    fun bind(channel: Channel) {
        channelName.setText(channel.name)
    }

    fun setActive(isActive: Boolean) {
        if (isActive) {
            rootView.setBackgroundColor(ThemeUtils.resolveAttribute(R.attr.colorPrimaryLight))
        } else {
            rootView.setBackgroundColor(Color.parseColor("#fcfcfc"))
        }
    }

    fun setUnreadCounter(unreadMessages: Int) {
        if (unreadMessages == 0) {
            toggleCounter(false)
        } else {
            toggleCounter(true)
            if (unreadMessages > 99) {
                unreadMessagesCounter.text = "99+"
            } else {
                unreadMessagesCounter.text = unreadMessages.toString()
            }
        }
    }

    private fun toggleCounter(isToggled: Boolean) {
        if (isToggled) {
            unreadMessagesCounter.visibility = View.VISIBLE
        } else {
            unreadMessagesCounter.visibility = View.INVISIBLE
        }
    }
}
