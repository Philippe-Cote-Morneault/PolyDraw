package com.log3900.chat.Message

import android.os.Handler
import com.log3900.chat.Channel.Channel
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.UserRepository
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

    fun getMessages(channel: Channel): LinkedList<ReceivedMessage> {
        return messageRepository.getChannelMessages(channel.ID, UserRepository.getUser().sessionToken, 0, 100)
    }

    fun sendMessage(channelID:UUID, message: String) {
        messageRepository.sendMessage(SentMessage(message, channelID))
    }

    private fun onMessageReceived(message: ReceivedMessage) {
        EventBus.getDefault().post(MessageEvent(EventType.RECEIVED_MESSAGE, message))
    }
}