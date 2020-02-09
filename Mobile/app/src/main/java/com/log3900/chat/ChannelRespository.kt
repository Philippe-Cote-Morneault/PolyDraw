package com.log3900.chat

import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ChannelRespository {
    fun getChannels(sessionToken: String): ArrayList<Channel> {
        val call = ChatRestService.service.getChannels(sessionToken, "EN")
        var channels: ArrayList<Channel>? = null
        call.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                val moshi = Moshi.Builder()
                    .add(UUIDAdapter())
                    .build()

                val adapter: JsonAdapter<ArrayList<Channel>> = moshi.adapter(Types.newParameterizedType(ArrayList::class.java, Channel::class.java))
                channels = adapter.fromJson(response.body().toString())
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
            }
        })

        return channels!!
    }

    fun getChannel(sessionToken: String, channelID: String) {
        var channel: Channel? = null
        val call = ChatRestService.service.getChannel(sessionToken, "EN", channelID)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val moshi = Moshi.Builder()
                    .add(UUIDAdapter())
                    .build()

                val adapter: JsonAdapter<Channel> = moshi.adapter(Channel::class.java)
                channel = adapter.fromJson(response.body().toString())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })
    }

    fun createChannel(channelName: String) {
        val dataObject = JsonObject()
        dataObject.addProperty("ChannelName", channelName)
        SocketService.instance?.sendJsonMessage(Event.CREATE_CHANNEL, dataObject.toString())
    }


}