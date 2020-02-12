package com.log3900.chat.Message

import com.log3900.chat.Channel.Channel
import com.log3900.user.UserRepository
import java.util.*

class MessageManager {
    private var messageRepository: MessageRepository

    constructor() {
        messageRepository = MessageRepository.instance!!
    }

    fun getMessages(channel: Channel): LinkedList<ReceivedMessage> {
        return messageRepository.getChannelMessages(channel.ID.toString(), UserRepository.getUser().sessionToken, 0, 100)
    }

    fun sendMessage(channelID:UUID, message: String) {
        messageRepository.sendMessage(SentMessage(message, channelID))
    }
}