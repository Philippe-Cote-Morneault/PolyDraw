package com.log3900.chat.Channel

import com.log3900.user.AccountRepository
import java.util.*
import kotlin.collections.ArrayList

class ChannelCache {
    var joinedChannels: ArrayList<Channel> = arrayListOf()
    var availableChannels: ArrayList<Channel> = arrayListOf()

    fun reloadChannels(channels: ArrayList<Channel>) {
        val username = AccountRepository.getAccount().username

        joinedChannels.clear()
        availableChannels.clear()

        for (channel in channels) {
            if (channel.users.find {
                    it.name == username
                } != null) {
                joinedChannels.add(channel)
            } else {
                availableChannels.add(channel)
            }
        }
    }

    fun addJoinedChannel(channel: Channel) {
        if (!joinedChannels.contains(channel)) {
            joinedChannels.add(channel)
        }
    }

    fun removeJoinedChannel(channel: Channel) {
        joinedChannels.remove(channel)
    }

    fun addAvailableChannel(channel: Channel) {
        if (!availableChannels.contains(channel)) {
            availableChannels.add(channel)
        }
    }

    fun removeAvailableChannel(channel: Channel) {
        availableChannels.remove(channel)
    }

    fun removeChannel(channelID: UUID) {
        var channelToRemove: Channel? = null
        channelToRemove = availableChannels.find {
            it.ID == channelID
        }

        if (channelToRemove != null) {
            removeAvailableChannel(channelToRemove)
            return
        }

        channelToRemove = joinedChannels.find {
            it.ID == channelID
        }

        if (channelToRemove != null) {
            removeJoinedChannel(channelToRemove)
            return
        }
    }
}