package com.log3900.chat.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.Message.EventMessage

class EventMessageViewHolder : RecyclerView.ViewHolder {
    private var messageTextView: TextView
    private lateinit var message: EventMessage

    constructor(itemView: View) : super(itemView) {
        messageTextView = itemView.findViewById(R.id.list_item_event_message_text_view_message)
    }

    fun bind(message: EventMessage) {
        this.message = message
        messageTextView.text = this.message.message
    }
}