package com.log3900.chat.Channel

import com.log3900.chat.ChatManager
import com.log3900.user.User
import com.log3900.user.UserRepository

class ChannelManager {
    private var user: User
    lateinit var activeChannel: Channel
    lateinit var availableChannels: ArrayList<Channel>
    lateinit var joinedChannels: ArrayList<Channel>

    constructor() {
        user = UserRepository.getUser()
    }

    fun init() {
        joinedChannels = ChannelRepository.instance?.getJoinedChannels(user.sessionToken)?.blockingGet()!!
        availableChannels = ChannelRepository.instance?.getAvailableChannels(user.sessionToken)?.blockingGet()!!
        activeChannel = joinedChannels.find {
            it.ID.toString() == "00000000-0000-0000-0000-000000000000"
        }!!
    }


}