package com.log3900.chat

import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonArray
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
}