package com.log3900.chat.Channel

import com.log3900.user.UserRepository

class ChannelCache {
    var joinedChannels: ArrayList<Channel> = arrayListOf()
    var availableChannels: ArrayList<Channel> = arrayListOf()
    var needsReload: Boolean = true

    constructor() {

    }

    fun reloadChannels(channels: ArrayList<Channel>) {
        val username = UserRepository.getUser().username

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
}