package com.log3900.socketServices

import java.util.*

data class MessageReceived(var message: String, var channelID:  UUID, var senderID: UUID, var senderName: String, var timestamp: Date)
data class MessageSent(var message: String, var channelID:  UUID)

enum class MessageEvent(var eventType: Int) {
    MESSAGE_RECEIVED(21),
    MESSAGE_SENT(20)
}