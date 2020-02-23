package com.log3900.chat

import com.log3900.chat.Message.EventMessage
import com.log3900.chat.Message.ReceivedMessage
import java.util.*

class ChatMessage(var type: Type, var message: Any, var channelID: UUID) {
    enum class Type {
        RECEIVED_MESSAGE,
        EVENT_MESSAGE
    }

    companion object {
        fun fromReceivedMessage(receivedMessage: ReceivedMessage): ChatMessage {
            return ChatMessage(Type.RECEIVED_MESSAGE, receivedMessage, receivedMessage.channelID)
        }

        fun fromReceivedMessages(receivedMessages: List<ReceivedMessage>): LinkedList<ChatMessage> {
            val messages: LinkedList<ChatMessage> = LinkedList()
            receivedMessages.forEach {
                messages.add(fromReceivedMessage(it))
            }

            return messages
        }

        fun fromEventMessage(eventMessage: EventMessage, channelID: UUID): ChatMessage {
            return ChatMessage(Type.EVENT_MESSAGE, eventMessage, channelID)
        }
    }
}