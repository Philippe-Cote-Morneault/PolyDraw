package com.log3900.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.Message
import java.util.*

class MessageAdapter(val messages: LinkedList<Message>) : RecyclerView.Adapter<MessageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false) as View

        return MessageViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}