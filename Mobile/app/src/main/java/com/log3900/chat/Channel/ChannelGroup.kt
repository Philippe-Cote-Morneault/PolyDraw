package com.log3900.chat.Channel

enum class GroupType {
    AVAILABLE,
    JOINED
}
class ChannelGroup(var type: GroupType, var channels: HashSet<Channel>) {
    fun getName(): String {
        when (type) {
            GroupType.AVAILABLE -> return "Available"
            GroupType.JOINED -> return "Joined"
        }
    }
}