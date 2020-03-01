package com.log3900.chat.Channel

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

enum class GroupType {
    AVAILABLE,
    JOINED
}
class ChannelGroup(var type: GroupType, var channels: ArrayList<Channel>, var unreadMessages: HashMap<UUID, Int>,
                   var filteredChannels: ArrayList<Channel> = arrayListOf(), var activeChannel: Channel? = null) {
    fun getName(): String {
        when (type) {
            GroupType.AVAILABLE -> return "Available"
            GroupType.JOINED -> return "Joined"
        }
    }
}