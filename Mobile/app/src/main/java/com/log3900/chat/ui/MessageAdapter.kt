package com.log3900.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.chat.Message
import java.util.*

class MessageAdapter(val messages: LinkedList<Message>) : RecyclerView.Adapter<MessageViewHolder>() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false) as View

        return MessageViewHolder(textView)
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
    fun prependMessage(message: Message) {
        messages.addFirst(message)
        notifyItemInserted(0)
    }

    /**
     * Adds a message after the most recent message.
     *
     * @param message the message to add
     */
    fun appendMessage(message: Message) {
        messages.addLast(message)
        notifyItemInserted(messages.size - 1)

        if (!recyclerView.canScrollVertically(1)) {
            recyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }
}
