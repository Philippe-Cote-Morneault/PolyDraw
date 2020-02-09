package com.log3900.chat.Channel

import com.log3900.user.UserRepository
import java.util.*
import kotlin.collections.ArrayList


class ChannelService {
    private var currentChannelID: UUID
    private var channelRepository: ChannelRepository
    private var channels: ArrayList<Channel> = arrayListOf()
    private var joinedChannels: ArrayList<Channel> = arrayListOf()

    constructor() {
        channelRepository = ChannelRepository.instance!!
        channels = channelRepository.getChannels(UserRepository.getUser().sessionToken)
        currentChannelID = UUID.fromString("0000-0000-0000-0000")
    }

    /*
    fun getChannels(token: String): Array<Channel>? {
        var channels: Array<Channel>? = null
        val res = ChatRestService.service.getChannels(token, "EN")
        res.enqueue(object : Callback<Array<Channel>> {
            override fun onResponse(call: Call<Array<Channel>>, response: Response<Array<Channel>>) {
                when(response.code()) {
                    200 -> channels = response.body()
                    else -> println(response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<Array<Channel>>, t: Throwable) {
                println(t.toString())
            }
        })

        return channels
    }

     */

}

