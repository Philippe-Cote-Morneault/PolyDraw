package com.log3900.chat

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
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

class ChatFragment : Fragment(), ChatView {
    // Services
    private lateinit var chatPresenter: ChatPresenter
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

        chatPresenter = ChatPresenter(this)

        return rootView
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
        sendMessageButton.setOnClickListener { v -> onSendMessageButtonClick(v)}

        setupToolbar(rootView)

        drawer = rootView.findViewById(R.id.fragment_chat_drawer_layout)

        rootView.findViewById<TextInputEditText>(R.id.fragment_chat_new_message_input).setOnClickListener{ v -> onMessageTextInputClick(v)}

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView.rootView.height - rootView.height
            val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, context?.resources?.displayMetrics)
            if (heightDiff > pixels) {
                chatPresenter.onKeyboardChange(true)
            }
            else {
                chatPresenter.onKeyboardChange(false)
            }
        }

    }

    private fun setupToolbar(rootView: View) {
        toolbar = rootView.findViewById(R.id.fragment_chat_top_layout)
        toolbar.setNavigationIcon(R.drawable.ic_hamburger_menu)

        toolbar.setNavigationOnClickListener {chatPresenter.handleNavigationDrawerClick()}
    }

    private fun setupMessagesRecyclerView(rootView: View) {
        messagesRecyclerView = rootView.findViewById(R.id.fragment_chat_message_recycler_view)
        messagesViewAdapter = MessageAdapter(LinkedList(), activity?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString(getString(R.string.preference_file_username_key), "nil")!!)
        messagesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = messagesViewAdapter
        }
    }

    private fun onSendMessageButtonClick(v: View) {
        val messageInput: TextInputEditText = v.rootView.findViewById(R.id.fragment_chat_new_message_input)
        val messageText = messageInput.text.toString()
        messageInput.text?.clear()
        if (messageText != "" && messageText.trim().length > 0)
        {
            chatPresenter.sendMessage(messageText.trim())
        }
    }

    private fun onMessageTextInputClick(v: View) {
        messagesViewAdapter.scrollToBottom()
    }

    override fun onResume() {
        super.onResume()
        chatPresenter.resume()
    }

    override fun openNavigationDrawer() {
        drawer.openDrawer(Gravity.LEFT)
    }

    override fun closeNavigationDrawer() {
        drawer.closeDrawer(Gravity.LEFT)
    }

    override fun isNavigationDrawerOpened(): Boolean {
        return drawer.isDrawerOpen(GravityCompat.START)
    }

    override fun notifyNewMessage() {
        messagesViewAdapter.messageInserted()
    }

    override fun setReceivedMessages(messages: LinkedList<ReceivedMessage>) {
        messagesViewAdapter.setMessage(messages)
    }

    override fun setCurrentChannnelName(name: String) {
        toolbar.setTitle(name)
    }

    override fun playNewMessageNotification() {
        val musicPlayer = MediaPlayer.create(this.context, R.raw.audio_notification_new_message)
        musicPlayer.start()
    }

    override fun scrollMessage() {
        messagesViewAdapter.scrollToBottom()
    }
}