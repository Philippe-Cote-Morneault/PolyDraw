package com.log3900.chat.Channel

import com.squareup.moshi.Json
import java.util.*
import kotlin.collections.ArrayList

class Channel(@Json(name = "ID") var ID: UUID, @Json(name = "Name") var name: String, @Json(name = "IsGame") var isGame: Boolean, @Json(name = "Users") var users: ArrayList<ChannelUser>) {
    companion object {
        val GENERAL_CHANNEL_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}

class ChannelUser(@Json(name = "Name") var name: String, @Json(name = "ID") var ID: UUID)