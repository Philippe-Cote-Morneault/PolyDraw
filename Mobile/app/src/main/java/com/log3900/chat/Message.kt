package com.log3900.chat

import java.sql.Time
import java.util.*

data class Message(var message: String, var channelID:  UUID, var senderID: UUID, var senderName: String, var timestamp: Date)