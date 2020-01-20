package com.log3900.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_chat, container, false)

        viewManager = LinearLayoutManager(activity)
        viewAdapter = MessageAdapter(ArrayList())
        recyclerView = rootView.findViewById(R.id.fragment_chat_message_recycler_view)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return rootView
    }

    class MessageAdapter(private val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        class MessageViewHolder : RecyclerView.ViewHolder {
            private var view: View
            private lateinit var message: Message

            constructor(itemView: View) : super(itemView) {
                view = itemView
            }

            fun bind(message: Message) {
                this.message = message
            }
        }

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
}