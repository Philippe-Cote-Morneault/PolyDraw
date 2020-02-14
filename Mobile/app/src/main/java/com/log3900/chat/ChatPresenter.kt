package com.log3900.chat

import android.os.Handler
import android.os.Message
import com.log3900.chat.Channel.Channel
import com.log3900.chat.Message.MessageRepository
import com.log3900.chat.Message.ReceivedMessage
import com.log3900.chat.Message.SentMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.shared.ui.ProgressDialog
import com.log3900.user.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ChatPresenter : Presenter {
    private var chatView: ChatView
    private lateinit var messageRepository: MessageRepository
    private lateinit var chatManager: ChatManager
    private var keyboardOpened: Boolean = false

    constructor(chatView: ChatView) {
        this.chatView = chatView
        if (!(ChatManager.instance?.ready!!)) {
            val progressDialog = ProgressDialog()
            chatView.showProgressDialog(progressDialog)
            ChatManager.instance?.subject?.filter {
                it
            }?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                chatView.hideProgressDialog(progressDialog)
                init()
            }
        } else {
            init()
        }

    }

    private fun init() {
        chatManager = ChatManager.instance!!
        chatView.setReceivedMessages(chatManager?.getCurrentChannelMessages().blockingGet())
        chatView.setCurrentChannnelName(ChatManager.instance?.getActiveChannel()?.name!!)
        messageRepository = MessageRepository.instance!!

        subscribeToEvents()
    }

    fun sendMessage(message: SentMessage) {
        messageRepository.sendMessage(message)
    }

    fun sendMessage(messageText: String) {
        chatManager.sendMessage(messageText)
    }

    fun handleNavigationDrawerClick() {
        if (chatView.isNavigationDrawerOpened()) {
            chatView.closeNavigationDrawer()
        } else {
            chatView.openNavigationDrawer()
        }
    }

    fun onKeyboardChange(opened: Boolean) {
        if (opened != keyboardOpened) {
            keyboardOpened = opened
            if (keyboardOpened) {
                chatView.scrollMessage()
            }
        }
    }

    private fun subscribeToEvents() {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.ACTIVE_CHANNEL_CHANGED -> {
                onChannelChanged(event.data as Channel)
            }
            EventType.RECEIVED_MESSAGE -> {
                onNewMessage(event.data as ReceivedMessage)
            }
        }
    }

    private fun onChannelChanged(channel: Channel) {
        chatManager.getCurrentChannelMessages().observeOn(AndroidSchedulers.mainThread()).subscribe(
            { messages ->
                chatView.setCurrentChannnelName(channel.name)
                chatView.setReceivedMessages(messages)
            },
            { error ->
            }
        )
    }

    private fun onNewMessage(message: ReceivedMessage) {
        chatView.notifyNewMessage()
        if (UserRepository.getUser().username != message.senderName) {
            chatView.playNewMessageNotification()
        }
    }

    override fun resume() {
        //chatView.setReceivedMessages(chatManager.getCurrentChannelMessages().blockingGet())
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}