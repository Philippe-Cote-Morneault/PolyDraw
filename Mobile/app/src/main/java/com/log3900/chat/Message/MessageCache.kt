package com.log3900.chat.Message

import com.log3900.chat.ChatMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MessageCache {
    val chatMessages: ConcurrentHashMap<UUID, LinkedList<ChatMessage>> = ConcurrentHashMap()

    fun createArrayForChannel(channelID: UUID) {
        if (!chatMessages.containsKey(channelID)) {
            chatMessages[channelID] = LinkedList<ChatMessage>()
        }
    }

    fun getMessages(channelID: UUID): LinkedList<ChatMessage> {
        if (!chatMessages.containsKey(channelID)) {
            createArrayForChannel(channelID)
        }

        return chatMessages[channelID]!!
    }

    fun appendMessage(message: ChatMessage) {
        if (!chatMessages.containsKey(message.channelID)) {
            createArrayForChannel(message.channelID)
        }
        chatMessages[message.channelID]?.addLast(message)
    }

    fun prependMessage(channelID: UUID, messages: LinkedList<ChatMessage>) {
        if (!this.chatMessages.containsKey(channelID)) {
            createArrayForChannel(channelID)
        }

        for (message in messages) {
            this.chatMessages[channelID]?.addFirst(message)
        }
    }

    fun removeEntry(channelID: UUID) {
        chatMessages.remove(channelID)
    }
}