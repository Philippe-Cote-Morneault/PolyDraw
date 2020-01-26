package com.log3900.chat

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.chat.ui.MessageAdapter
import com.log3900.utils.ui.KeyboardHelper
import java.lang.Thread.sleep
import java.util.*


var username = "admin"

class ChatFragment : Fragment() {
    // Services
    private lateinit var messageService: MessageService
    // UI elements
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messagesViewAdapter: MessageAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var sendMessageButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_chat, container, false)

        setupUiElements(rootView)

        messageService = MessageService()

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
        }

        thread.start()



        val handler = Handler(object: Handler.Callback {
            override fun handleMessage(msg: android.os.Message): Boolean {
                println("inside callback, tid = " + Thread.currentThread().id)
                println(msg.toString())

                messagesViewAdapter.messages.addLast(msg.obj as Message)
                messagesViewAdapter.notifyItemInserted(messagesViewAdapter.messages.size - 1)
                if (!messagesRecyclerView.canScrollVertically(1)) {
                    messagesRecyclerView.smoothScrollToPosition(messagesViewAdapter.messages.size - 1)
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

        return rootView
    }

    inner class SendMessageButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            val messageInput: TextInputEditText = v.rootView.findViewById(R.id.fragment_chat_new_message_input)
            val messageText = messageInput.text.toString()
            messageInput.text?.clear()
            messageService.sendMessage(messageText)

            KeyboardHelper.hideKeyboard(activity as Activity)

        }
    }


    /**
     * Used to assign all variables holding UI elements for this fragment and setup listeners.
     *
     * @param rootView the root view of this fragment
     */
    private fun setupUiElements(rootView: View) {
        viewManager = LinearLayoutManager(activity)

        setupMessagesRecyclerView(rootView)

        sendMessageButton = rootView.findViewById(R.id.fragment_chat_send_message_button)
        sendMessageButton.setOnClickListener(SendMessageButtonListener())

        setupToolbar(rootView)

        drawer = rootView.findViewById(R.id.fragment_chat_drawer_layout)
    }

    private fun setupToolbar(rootView: View) {
        toolbar = rootView.findViewById(R.id.fragment_chat_top_layout)
        toolbar.inflateMenu(R.menu.fragment_chat_top_menu)
        toolbar.setNavigationIcon(R.drawable.ic_hamburger_menu)

        toolbar.setNavigationOnClickListener {onToolbarNavigationClick()}
    }

    private fun setupMessagesRecyclerView(rootView: View) {
        messagesRecyclerView = rootView.findViewById(R.id.fragment_chat_message_recycler_view)
        messagesViewAdapter = MessageAdapter(LinkedList())
        messagesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = messagesViewAdapter
        }
    }

    /**
     * Handles click on the top toolbar navigation icon. Closes and opens the navigation drawer, which contains the channel list.
     *
     */
    private fun onToolbarNavigationClick() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(Gravity.LEFT)
        } else {
            drawer.openDrawer(Gravity.LEFT)
        }
    }
}