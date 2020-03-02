package com.log3900.chat.Message

import android.os.Handler
import android.util.Log
import com.log3900.chat.Channel.Channel
import com.log3900.chat.ChatMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Message
import io.reactivex.Single
import org.greenrobot.eventbus.EventBus
import java.util.*

class MessageManager {
    private var messageRepository: MessageRepository? = null
    private lateinit var socketMessageHandler: Handler

    constructor() {
        messageRepository = MessageRepository.instance!!
    }

    fun init() {
        socketMessageHandler = messageRepository!!.subscribe(MessageRepository.Event.CHAT_MESSAGE_RECEIVED,Handler{
            onMessageReceived(it.obj as ChatMessage)
            true
        })
    }

    fun delete() {
        messageRepository?.unsubscribe(MessageRepository.Event.CHAT_MESSAGE_RECEIVED, socketMessageHandler)
        messageRepository = null
    }

    fun getMessages(channel: Channel): Single<LinkedList<ChatMessage>> {
        return messageRepository!!.getChannelMessages(channel.ID)
    }

    fun sendMessage(channelID:UUID, message: String) {
        messageRepository?.sendMessage(SentMessage(message, channelID))
    }

    fun loadMoreMessages(channelID: UUID): Single<Int> {
        return messageRepository!!.loadMoreMessages(15, channelID)
    }

    private fun onMessageReceived(message: ChatMessage) {
        EventBus.getDefault().post(MessageEvent(EventType.RECEIVED_MESSAGE, message))
    }
}