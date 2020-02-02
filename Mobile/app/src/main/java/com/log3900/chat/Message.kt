package com.log3900.chat

import com.squareup.moshi.Json
import java.sql.Time
import java.util.*

data class ReceivedMessage(@Json(name = "Message") var message: String, @Json(name = "ChannelID") var channelID:  String, @Json(name = "SenderID") var senderID: String,
                           @Json(name = "SenderName") var senderName: String, @Json(name = "TimeStamp") var timestamp: Date)

data class SentMessage(@Json(name = "Message")var message: String, @Json(name = "ChannelID") var channelID: String)