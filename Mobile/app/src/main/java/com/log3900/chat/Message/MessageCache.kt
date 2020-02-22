package com.log3900.chat.Message

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MessageCache {
    val messages: ConcurrentHashMap<UUID, LinkedList<ReceivedMessage>> = ConcurrentHashMap()

    fun createArrayForChannel(channelID: UUID) {
        if (!messages.containsKey(channelID)) {
            messages[channelID] = LinkedList<ReceivedMessage>()
        }
    }

    fun getMessages(channelID: UUID): LinkedList<ReceivedMessage> {
        if (!messages.containsKey(channelID)) {
            createArrayForChannel(channelID)
        }

        return messages[channelID]!!
    }

    fun appendMessage(message: ReceivedMessage) {
        if (!messages.containsKey(message.channelID)) {
            createArrayForChannel(message.channelID)
        }
        messages[message.channelID]?.addLast(message)
    }

    fun prependMessage(channelID: UUID, messages: LinkedList<ReceivedMessage>) {
        if (!this.messages.containsKey(channelID)) {
            createArrayForChannel(channelID)
        }

        for (message in messages) {
            this.messages[channelID]?.addFirst(message)
        }
    }
}