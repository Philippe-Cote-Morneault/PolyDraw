package com.log3900.chat.Message

import android.os.Handler
import com.log3900.chat.Channel.Channel
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.UserRepository
import io.reactivex.Single
import org.greenrobot.eventbus.EventBus
import java.util.*

class MessageManager {
    private var messageRepository: MessageRepository

    constructor() {
        messageRepository = MessageRepository.instance!!
    }

    fun init() {
        messageRepository.subscribe(MessageRepository.Event.MESSAGE_RECEIVED, Handler {
            onMessageReceived(it.obj as ReceivedMessage)
            true
        })
    }

    fun getMessages(channel: Channel): Single<LinkedList<ReceivedMessage>> {
        return messageRepository.getChannelMessages(channel.ID)
    }

    fun sendMessage(channelID:UUID, message: String) {
        messageRepository.sendMessage(SentMessage(message, channelID))
    }

    fun loadMoreMessages(channelID: UUID) {
        messageRepository.loadMoreMessages(25, channelID)
    }

    private fun onMessageReceived(message: ReceivedMessage) {
        EventBus.getDefault().post(MessageEvent(EventType.RECEIVED_MESSAGE, message))
    }
}