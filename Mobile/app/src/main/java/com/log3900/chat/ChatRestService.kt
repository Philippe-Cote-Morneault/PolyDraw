package com.log3900.chat

import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ChatRestService {
    @GET("chat/channels")
    fun getChannels(@Header("SessionToken") sessionToken: String, @Header("Language") language: String): Call<Array<Channel>>

    @GET("chat/channels/{channelID}")
    fun getChannel(@Header("SessionToken") sessionToken: String, @Header("Language") language: String, @Path("channelID") channelID: String): Call<Channel>

    @GET("chat/messages/{channelID}/?start={start}&end={end}")
    fun getChannelMessages(@Header("SessionToken") sessionToken: String, @Header("Language") language: String,
                           @Path("channelID") channelID: String, @Path("start") start: Int, @Path("end") end: Int): Call<ChannelMessages>
    
}

data class ChannelMessages(@Json(name = "Messages") var messages: Array<ReceivedMessage>, @Json(name = "MessagesTottal") var messagesTotal: Int)

