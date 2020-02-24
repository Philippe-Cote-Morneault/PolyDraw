package com.log3900.chat.ui

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.ChatMessage
import com.log3900.chat.Message.ReceivedMessage
import com.log3900.utils.format.DateFormatter

class ReceivedMessageViewHolder : RecyclerView.ViewHolder {
    private var view: ConstraintLayout
    private var messageTextView: TextView
    private var usernameTextView: TextView
    private var dateTextView: TextView
    private var username: String
    private var messageBoxCardView: CardView
    private lateinit var message: ReceivedMessage

    constructor(itemView: View, username: String) : super(itemView) {
        view = itemView.findViewById(R.id.list_item_message_outer_layout)
        messageTextView = itemView.findViewById(R.id.list_item_message_text)
        usernameTextView = itemView.findViewById(R.id.list_item_message_header)
        dateTextView = itemView.findViewById(R.id.list_item_message_date)
        messageTextView.maxLines = Integer.MAX_VALUE
        messageBoxCardView = itemView.findViewById(R.id.list_item_message_text_card_view)
        this.username = username
    }

    fun bind(message: ChatMessage) {
        this.message = message.message as ReceivedMessage
        messageTextView.text = this.message.message
        usernameTextView.text = this.message.username
        dateTextView.text = DateFormatter.formatDate(this.message.timestamp)

        val constraintSet = ConstraintSet()
        constraintSet.clone(view)

        if (this.message.username == username) {
            constraintSet.clear(R.id.list_item_message_inner_layout, ConstraintSet.START)
            constraintSet.connect(R.id.list_item_message_inner_layout, ConstraintSet.END, R.id.list_item_message_outer_layout, ConstraintSet.END, 15)
            messageTextView.setBackgroundColor(Color.parseColor("#3F51B5"))
            messageTextView.setTextColor(Color.parseColor("#FFFFFF"))
            messageTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            view.findViewById<LinearLayout>(R.id.list_item_message_inner_layout).gravity = Gravity.END
            usernameTextView.visibility = View.GONE
            usernameTextView.setTextColor(Color.parseColor("#FFFFFF"))
            usernameTextView.gravity = Gravity.END
            dateTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        }
        else {
            constraintSet.clear(R.id.list_item_message_inner_layout, ConstraintSet.END)
            constraintSet.connect(R.id.list_item_message_inner_layout, ConstraintSet.START, R.id.list_item_message_outer_layout, ConstraintSet.START, 15)
            messageTextView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            messageTextView.setTextColor(Color.parseColor("#000000"))
            messageTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            view.findViewById<LinearLayout>(R.id.list_item_message_inner_layout).gravity = Gravity.START
            usernameTextView.visibility = View.VISIBLE
            usernameTextView.setTextColor(Color.parseColor("#000000"))
            dateTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }

        constraintSet.applyTo(view)
    }
}