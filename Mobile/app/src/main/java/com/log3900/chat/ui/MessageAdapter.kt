package com.log3900.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.ChatMessage
import com.log3900.chat.Message.EventMessage
import java.util.*

class MessageAdapter(var messages: LinkedList<ChatMessage>, val username: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var recyclerView: RecyclerView

    fun setMessage(messages: LinkedList<ChatMessage>) {
        this.messages = messages
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        if (viewType == ChatMessage.Type.RECEIVED_MESSAGE.ordinal) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false) as View
            return ReceivedMessageViewHolder(view, username)
        } else if (viewType == ChatMessage.Type.EVENT_MESSAGE.ordinal) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_event_message, parent, false) as View
            return EventMessageViewHolder(view)
        } else {
            throw IllegalArgumentException("ViewHolder type not found!")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ChatMessage.Type.RECEIVED_MESSAGE.ordinal) {
            (holder as ReceivedMessageViewHolder).bind(messages[position])
        } else if (getItemViewType(position) == ChatMessage.Type.EVENT_MESSAGE.ordinal) {
            (holder as EventMessageViewHolder).bind(messages[position].message as EventMessage)
        }
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
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }
}