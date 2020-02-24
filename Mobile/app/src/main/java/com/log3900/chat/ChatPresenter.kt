package com.log3900.chat

import com.log3900.chat.Channel.Channel
import com.log3900.chat.Message.MessageRepository
import com.log3900.chat.Message.ReceivedMessage
import com.log3900.chat.Message.SentMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.shared.ui.ProgressDialog
import com.log3900.user.AccountRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChatPresenter : Presenter {
    private var chatView: ChatView
    private lateinit var messageRepository: MessageRepository
    private lateinit var chatManager: ChatManager
    private var keyboardOpened: Boolean = false
    private var loadingMessages: Boolean

    constructor(chatView: ChatView) {
        this.chatView = chatView
        loadingMessages = false
        ChatManager.getInstance()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            {
                chatManager = it
                init()
            },
            {

            }
        )

    }

    private fun init() {
        chatManager.getCurrentChannelMessages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    chatView.setChatMessages(it)
                    chatView.scrollMessage()
                },
                {

                }
            )
        chatView.setCurrentChannnelName(chatManager.getActiveChannel().name)
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

    fun scrolledToPositions(firstPosition: Int, lastPosition: Int, scrollDirection: Int) {
        if (scrollDirection < 0) {
            if (firstPosition < 15 && !loadingMessages) {
                loadingMessages = true
                chatManager.loadMoreMessages().subscribe(
                    {
                        if (it > 0) {
                            chatView.notifyMessagesPrepended(it)
                        }
                        loadingMessages = false
                    },
                    {
                    }
                )
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
            EventType.ACTIVE_CHANNEL_MESSAGE_RECEIVED -> {
                onActiveChannelMessageReceived(event.data as ChatMessage)
            }
        }
    }

    private fun onChannelChanged(channel: Channel) {
        chatManager.getCurrentChannelMessages().observeOn(AndroidSchedulers.mainThread()).subscribe(
            { messages ->
                chatView.setCurrentChannnelName(channel.name)
                chatView.setChatMessages(messages)
                chatView.scrollMessage()
            },
            { error ->
            }
        )
    }

    private fun onActiveChannelMessageReceived(message: ChatMessage) {
        chatView.notifyNewMessage()
        if (message.type == ChatMessage.Type.RECEIVED_MESSAGE && AccountRepository.getAccount().username != (message.message as ReceivedMessage).username) {
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