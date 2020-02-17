package com.log3900.chat

import com.log3900.chat.Message.EventMessage
import com.log3900.chat.Message.ReceivedMessage

class ChatMessage(var type: Type, var receivedMessage: ReceivedMessage?, var eventMessage: EventMessage?) {
    enum class Type {
        RECEIVED_MESSAGE,
        EVENT_MESSAGE
    }
}