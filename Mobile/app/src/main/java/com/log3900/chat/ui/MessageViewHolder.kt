package com.log3900.chat.ui

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.Message
import com.log3900.chat.username

class MessageViewHolder : RecyclerView.ViewHolder {
    private var view: ConstraintLayout
    private var messageTextView: TextView
    private var usernameTextView: TextView
    private var dateTextView: TextView
    private lateinit var message: Message

    constructor(itemView: View) : super(itemView) {
        view = itemView.findViewById(R.id.list_item_message_outer_layout)
        messageTextView = itemView.findViewById(R.id.list_item_message_text)
        usernameTextView = itemView.findViewById(R.id.list_item_message_username)
        dateTextView = itemView.findViewById(R.id.list_item_message_date)
    }

    fun bind(message: Message) {
        this.message = message
        messageTextView.text = message.message
        usernameTextView.text = message.senderName
        dateTextView.text = message.timestamp.toString()

        var constraintSet = ConstraintSet()
        constraintSet.clone(view)

        if (message.senderName.equals(username)) {
            constraintSet.clear(R.id.list_item_message_inner_layout, ConstraintSet.START)
            constraintSet.connect(R.id.list_item_message_inner_layout, ConstraintSet.END, R.id.list_item_message_outer_layout, ConstraintSet.END, 15)
        }
        else {
            constraintSet.clear(R.id.list_item_message_inner_layout, ConstraintSet.END)
            constraintSet.connect(R.id.list_item_message_inner_layout, ConstraintSet.START, R.id.list_item_message_outer_layout, ConstraintSet.START, 15)
        }

        constraintSet.applyTo(view)
    }
}