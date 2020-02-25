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
                    it.ID == AccountRepository.getAccount().userID
                } != null) {
                addJoinedChannel(channel)
            } else {
                addAvailableChannel(channel)
            }
        }
    }

    fun addJoinedChannel(channel: Channel) {
        if (!joinedChannels.contains(channel)) {
            var index = Collections.binarySearch(joinedChannels, channel, ChannelAlphabeticalComparator())
            if (index < 0) {
                index = -index - 1
            }
            joinedChannels.add(index, channel)
        }
    }

    fun removeJoinedChannel(channel: Channel) {
        joinedChannels.remove(channel)
    }

    fun addAvailableChannel(channel: Channel) {
        if (!availableChannels.contains(channel)) {
            var index = Collections.binarySearch(availableChannels, channel, ChannelAlphabeticalComparator())
            if (index < 0) {
                index = -(index + 1)
            }
            availableChannels.add(index, channel)
        }
    }

    fun removeAvailableChannel(channel: Channel) {
        availableChannels.remove(channel)
    }

    fun removeChannel(channelID: UUID) {
        var channelToRemove = availableChannels.find {
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

    inner class ChannelAlphabeticalComparator : Comparator<Channel> {
        override fun compare(channel1: Channel, channel2: Channel): Int {
            if (channel1.ID.equals(Channel.GENERAL_CHANNEL_ID)) {
                return -1
            } else if (channel2.ID.equals(Channel.GENERAL_CHANNEL_ID)) {
                return 1
            } else {
                return channel1.name.compareTo(channel2.name)
            }
        }
    }
}