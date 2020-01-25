package com.log3900.chat

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.chat.ui.MessageAdapter
import com.log3900.utils.ui.KeyboardHelper
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.ArrayList





var username = "admin"

class ChatFragment : Fragment() {
    private lateinit var messageService: MessageService
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MessageAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var sendMessageButton: Button
    public lateinit var handler: Handler


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_chat, container, false)

        messageService = MessageService()


        println("tid of ChatFragment = " + Thread.currentThread().id)

        val thread = Thread{
            var i = 1
            while (true) {
                sleep(3000)
                println("notifying, tid = " + Thread.currentThread().id)
                val tempMessage = android.os.Message()
                tempMessage.obj = Message("nessage" + i, UUID.randomUUID(), UUID.randomUUID(), "username" + i, Date())
                tempMessage.what = MessageEvent.MESSAGE_RECEIVED.ordinal
                messageService.notifySubscribers(MessageEvent.MESSAGE_RECEIVED,tempMessage)
                ++i
            }
           // Looper.prepare()
            //val hand = Handler()
            //println("tid of new thread = " + Thread.currentThread().id)
        }

        thread.start()



        handler = Handler(object: Handler.Callback {
            override fun handleMessage(msg: android.os.Message): Boolean {
                println("inside callback, tid = " + Thread.currentThread().id)
                println(msg.toString())

                viewAdapter.messages.addLast(msg.obj as Message)
                viewAdapter.notifyItemInserted(viewAdapter.messages.size - 1)
                if (!recyclerView.canScrollVertically(1)) {
                    recyclerView.smoothScrollToPosition(viewAdapter.messages.size - 1)
                }
                return true
            }
        })

        messageService.subscribe(MessageEvent.MESSAGE_RECEIVED, handler)

        val messagesTest: LinkedList<Message> = LinkedList()
        messagesTest.add(Message("user send this 1", UUID.randomUUID(), UUID.randomUUID(), "admin", Date()))
        for (i in 0..10) {
            messagesTest.add(Message("test" + i, UUID.randomUUID(), UUID.randomUUID(), "sender" + i, Date()))
        }

        messagesTest.add(Message("user send this 2", UUID.randomUUID(), UUID.randomUUID(), "admin", Date()))
        viewManager = LinearLayoutManager(activity)
        viewAdapter = MessageAdapter(messagesTest)
        recyclerView = rootView.findViewById(R.id.fragment_chat_message_recycler_view)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        sendMessageButton = rootView.findViewById(R.id.fragment_chat_send_message_button)
        sendMessageButton.setOnClickListener(SendMessageButtonListener())


        //messageService = MessageService()

        return rootView
    }

    inner class SendMessageButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            val messageInput: TextInputEditText = v.rootView.findViewById(R.id.fragment_chat_new_message_input)
            //this@ChatFragment.messageService.sendMessage(Message(messageInput.text.toString(), UUID.randomUUID(), UUID.randomUUID(), "sender1", Date()))
            messageInput.text?.clear()
            var runnable = Runnable{ println(Date().toString()) }

            var msg = android.os.Message()
            msg.what = 1
            handler.sendMessage(msg)

            KeyboardHelper.hideKeyboard(activity as Activity)

            //handler.post(runnable)

        }
    }
}