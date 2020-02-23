package com.log3900.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.ChatMessage
import java.util.*

class MessageAdapter(var messages: LinkedList<ChatMessage>, val username: String) : RecyclerView.Adapter<ReceivedMessageViewHolder>() {
    private lateinit var recyclerView: RecyclerView

    fun setMessage(messages: LinkedList<ChatMessage>) {
        this.messages = messages
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivedMessageViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false) as View
        return ReceivedMessageViewHolder(textView, username)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: ReceivedMessageViewHolder, position: Int) {
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
    fun prependMessages(count: Int) {
        notifyItemRangeInserted(0, count)
    }

    /**
     * Adds a message after the most recent message.
     *
     * @param message the message to add
     */

    fun messageInserted() {
        notifyItemInserted(messages.size - 1)
        if (!recyclerView.canScrollVertically(1)) {
            recyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }

    fun isScrolledToBottom(): Boolean {
        return !recyclerView.canScrollVertically(1)
    }

    fun scrollToBottom() {
        recyclerView.scrollToPosition(messages.size - 1)
    }
}