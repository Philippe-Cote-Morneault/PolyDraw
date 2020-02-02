package com.log3900.chat

import java.sql.Time
import java.util.*

data class ReceivedMessage(var message: String, var channelID:  String, var senderID: String, var senderName: String, var timestamp: Date)

data class SentMessage(var message: String, var channelID: String)