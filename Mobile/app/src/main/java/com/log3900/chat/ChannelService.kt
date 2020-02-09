package com.log3900.chat

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChannelService {

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

}

