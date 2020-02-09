package com.log3900.chat

import android.os.Handler
import android.os.Message
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.user.UserRepository
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ChatPresenter : Presenter {
    private var chatView: ChatView
    private var messageRepository: MessageRepository
    private var chatManager: ChatManager
    private var keyboardOpened: Boolean

    constructor(chatView: ChatView) {
        this.chatView = chatView
        chatManager = ChatManager()
        messageRepository = MessageRepository.instance!!
        keyboardOpened = false

        subscribeToEvents()
    }

    fun sendMessage(message: SentMessage) {
        messageRepository.sendMessage(message)
    }

    fun sendMessage(messageText: String) {
        val message = SentMessage(messageText, UUID.randomUUID())
        sendMessage(message)
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
        messageRepository.subscribe(MessageRepository.Event.MESSAGE_RECEIVED, Handler {
            handleNewMessage(it)
            true
        })

        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.CHANGED_CHANNEL -> {
                onChannelChanged(event.data as Channel)
            }
        }
    }

    private fun onChannelChanged(channel: Channel) {
        chatView.setCurrentChannnelName(channel.name)
    }

    private fun handleNewMessage(message: Message) {
        val receivedMessage = message.obj as ReceivedMessage
        chatView.notifyNewMessage()
        if (UserRepository.getUser().username != receivedMessage.senderName) {
            chatView.playNewMessageNotification()
        }
    }

    override fun resume() {
        chatManager.test().subscribe {
            chatView.setReceivedMessages(it!!)
        }
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}