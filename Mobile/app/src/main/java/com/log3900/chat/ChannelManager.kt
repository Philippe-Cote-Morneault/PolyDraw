package com.log3900.chat

import com.log3900.user.User
import com.log3900.user.UserRepository

class ChannelManager {
    private var user: User
    //private var channels: ArrayList<Channel>

    constructor() {
        user = UserRepository.getUser()
        //channels = ChannelRepository.getChannels(user.sessionToken)
    }


}