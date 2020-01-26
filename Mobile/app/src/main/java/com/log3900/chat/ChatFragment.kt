package com.log3900.chat

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
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
           // Looper.prepare()
            //val hand = Handler()
            //println("tid of new thread = " + Thread.currentThread().id)
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

        //messageService = MessageService()

        setupToolbar()

        return rootView
    }

    fun setupToolbar() {
        //(activity as AppCompatActivity).setSupportActionBar(toolbar)
        //(activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        //(activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator()
        //setHasOptionsMenu(true)
       // onCreateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        println("INFLATING MENU")
        toolbar.inflateMenu(R.menu.fragment_chat_top_menu)
        //inflater.inflate(R.menu.fragment_chat_top_menu, menu)
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

    private fun setupUiElements(rootView: View) {
        viewManager = LinearLayoutManager(activity)
        messagesRecyclerView = rootView.findViewById(R.id.fragment_chat_message_recycler_view)
        messagesViewAdapter = MessageAdapter(LinkedList())
        messagesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = messagesViewAdapter
        }

        sendMessageButton = rootView.findViewById(R.id.fragment_chat_send_message_button)
        sendMessageButton.setOnClickListener(SendMessageButtonListener())

        toolbar = rootView.findViewById(R.id.fragment_chat_top_layout)
        toolbar.inflateMenu(R.menu.fragment_chat_top_menu)
        toolbar.setNavigationIcon(R.drawable.ic_hamburger_menu)

        toolbar.setNavigationOnClickListener {toolbarNavigationClick()}

        drawer = rootView.findViewById(R.id.fragment_chat_drawer_layout)
    }
    private fun toolbarNavigationClick() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(Gravity.LEFT)
        } else {
            drawer.openDrawer(Gravity.LEFT)
        }
    }
}