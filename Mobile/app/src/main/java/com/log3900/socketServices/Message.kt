package com.log3900.socketServices

import java.util.*

data class Message(var message: String, var channelID:  UUID, var senderID: UUID, var senderName: String, var timestamp: Date)
