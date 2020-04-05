package com.log3900.chat.Message

import com.log3900.MainApplication
import com.log3900.R
import com.log3900.chat.ChatMessage
import com.log3900.settings.language.LanguageManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MessageCache {
    val chatMessages: ConcurrentHashMap<UUID, LinkedList<ChatMessage>> = ConcurrentHashMap()
    private val loadedHistory: ConcurrentHashMap<UUID, Boolean> = ConcurrentHashMap()

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
        loadedHistory.remove(channelID)
    }

    fun isHistoryFetched(channelID: UUID): Boolean {
        if (!loadedHistory.containsKey(channelID)) {
            loadedHistory[channelID] = false
        }

        return loadedHistory[channelID]!!
    }

    fun setHistoryFetchedState(channelID: UUID, isLoaded: Boolean) {
        loadedHistory[channelID] = isLoaded
    }

    fun changeEventMessagesToLanguage(languageCode: String) {
        chatMessages.forEach { channel: UUID, channelMessages: LinkedList<ChatMessage> ->
            channelMessages.forEach {
                if (it.type == ChatMessage.Type.EVENT_MESSAGE) {
                    val oldEventMessage = it.message as EventMessage
                    val username = oldEventMessage.message.substring(0, oldEventMessage.message.indexOf(" "))
                    if (oldEventMessage.message.contains("left") || oldEventMessage.message.contains("quitté")) {
                        oldEventMessage.message = MainApplication.instance.getContext().getString(R.string.chat_user_left_channel_message, username)
                    } else {
                        oldEventMessage.message = MainApplication.instance.getContext().getString(R.string.chat_user_joined_channel_message, username)
                    }
                }
            }
        }
    }
}