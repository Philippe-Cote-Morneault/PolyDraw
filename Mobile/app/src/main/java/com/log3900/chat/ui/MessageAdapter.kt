package com.log3900.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.ReceivedMessage
import java.util.*

class MessageAdapter(val messages: LinkedList<ReceivedMessage>, val username: String) : RecyclerView.Adapter<MessageViewHolder>() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false) as View
        return MessageViewHolder(textView, username)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    /**
     * Adds a message before the oldest nessage.
     *
     * @param message the message to add
     */
    fun prependMessage(message: ReceivedMessage) {
        messages.addFirst(message)
        notifyItemInserted(0)
    }

    /**
     * Adds a message after the most recent message.
     *
     * @param message the message to add
     */
    fun appendMessage(message: ReceivedMessage) {
        messages.addLast(message)
        notifyItemInserted(messages.size - 1)

        if (!recyclerView.canScrollVertically(1)) {
            recyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }

    fun isScrolledToBottom(): Boolean {
        return !recyclerView.canScrollVertically(1)
    }

    fun scrollToBottom() {
        //recyclerView.smoothScrollToPosition(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }
}