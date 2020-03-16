package com.log3900.chat.Channel

import com.log3900.user.account.AccountRepository
import java.util.*

class ChannelCache {
    private var gameChannel: Channel? = null
    var joinedChannels: ArrayList<Channel> = arrayListOf()
    var availableChannels: ArrayList<Channel> = arrayListOf()

    fun reloadChannels(channels: ArrayList<Channel>) {
        joinedChannels.clear()
        availableChannels.clear()

        for (channel in channels) {
            if (channel.users.find {
                    it.ID == AccountRepository.getInstance().getAccount().ID
                } != null) {
                addJoinedChannel(channel)
            } else {
                addAvailableChannel(channel)
            }
        }
    }

    fun getChannel(channelID: UUID): Channel? {
        var channel = availableChannels.find {
            it.ID == channelID
        }

        if (channel == null) {
            channel = joinedChannels.find {
                it.ID == channelID
            }

        }

        return channel
    }

    fun addJoinedChannel(channel: Channel) {
        if (!joinedChannels.contains(channel)) {
            var index = Collections.binarySearch(joinedChannels, channel, ChannelAlphabeticalComparator())
            if (index < 0) {
                index = -index - 1
            }
            joinedChannels.add(index, channel)
        }

        if (channel.isGame) {
            gameChannel = channel
        }
    }

    fun removeJoinedChannel(channel: Channel) {
        if (hasGameChannel() && gameChannel?.ID == channel.ID) {
            gameChannel = null
        }

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

        if (channel.isGame) {
            gameChannel = channel
        }
    }

    fun removeAvailableChannel(channel: Channel) {
        if (hasGameChannel() && gameChannel?.ID == channel.ID) {
            gameChannel = null
        }

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

    fun hasGameChannel(): Boolean {
        return gameChannel != null
    }

    fun getGameChannel(): Channel? {
        return gameChannel
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