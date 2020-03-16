package com.log3900.chat.Channel

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.*
import kotlin.collections.ArrayList

object JsonAdapter {


    fun jsonToChannel(jsonObject: JsonObject): Channel {
        val id = UUID.fromString(jsonObject.get("ID").asString)
        val name = jsonObject.get("Name").asString
        val users = jsonToChannelUsers(jsonObject.get("Users").asJsonArray)
        var isGame = false
        if (jsonObject.has("IsGame")) {
            isGame = jsonObject.get("IsGame").asBoolean
        }

        return Channel(id, name, isGame, users)
    }

    fun jsonToChannels(jsonArray: JsonArray): ArrayList<Channel> {
        val channels = arrayListOf<Channel>()

        jsonArray.forEach {
            channels.add(jsonToChannel(it.asJsonObject))
        }

        return channels
    }

    fun jsonToChannelUser(jsonObject: JsonObject): ChannelUser {
        val name = jsonObject.get("Name").asString
        val id = UUID.fromString(jsonObject.get("ID").asString)

        return ChannelUser(name, id)

    }

    fun jsonToChannelUsers(jsonArray: JsonArray): ArrayList<ChannelUser> {
        val users = arrayListOf<ChannelUser>()

        jsonArray.forEach {
            users.add(jsonToChannelUser(it.asJsonObject))
        }

        return users
    }
}