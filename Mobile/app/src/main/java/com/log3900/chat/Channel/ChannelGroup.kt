package com.log3900.chat.Channel

import android.content.res.Resources
import com.log3900.MainApplication
import com.log3900.R
import java.util.*

enum class GroupType {
    AVAILABLE,
    JOINED
}
class ChannelGroup(var type: GroupType, var channels: ArrayList<Channel>, var unreadMessages: HashMap<UUID, Int>,
                   var filteredChannels: ArrayList<Channel> = arrayListOf(), var activeChannel: Channel? = null) {
    fun getName(): String {
        when (type) {
            GroupType.AVAILABLE -> return MainApplication.instance.getContext().getString(R.string.channel_available)
            GroupType.JOINED -> return MainApplication.instance.getContext().getString(R.string.channel_joined)
        }
    }
}