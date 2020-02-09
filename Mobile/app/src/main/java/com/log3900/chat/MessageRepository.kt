package com.log3900.chat

import android.os.Handler
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonObject
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.Call
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import retrofit2.Callback
import retrofit2.Response


object MessageRepository {
    fun getChannelMessages(channelID: String, sessionToken: String, startIndex: Int, endIndex: Int): ArrayList<ReceivedMessage> {
        val call = ChatRestService.service.getChannelMessages(sessionToken, "EN", channelID, startIndex, endIndex)
        var messages: ArrayList<ReceivedMessage>? = null
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when(response.code()) {
                    200 -> {
                        val moshi = Moshi.Builder()
                            .add(UUIDAdapter())
                            .build()
                        val adapter: JsonAdapter<ArrayList<ReceivedMessage>> = moshi.adapter(Types.newParameterizedType(ArrayList::class.java, ReceivedMessage::class.java))
                        messages = adapter.fromJson(response.body()!!.getAsJsonArray("Messages").toString())
                    }
                    else -> {

                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })

        return messages!!
    }
}