package com.log3900.chat.Channel

import com.log3900.user.User
import com.squareup.moshi.Json
import java.util.*

class Channel(@Json(name = "ID") var ID: UUID, @Json(name = "Name") var name: String, @Json(name = "Users") var users: Array<ChannelUser>) {
    companion object {
        val GENERAL_CHANNEL_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}

class ChannelUser(@Json(name = "Name") var name: String, @Json(name = "ID") var ID: UUID)