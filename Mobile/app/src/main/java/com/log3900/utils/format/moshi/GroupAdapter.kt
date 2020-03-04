package com.log3900.utils.format.moshi

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.game.group.Difficulty
import com.log3900.game.group.Group
import com.log3900.game.group.MatchMode
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*
import kotlin.collections.ArrayList

class GroupAdapter {
    @FromJson
    fun fromJson(groupJson: JsonObject): Group {
        return Group(
            UUID.fromString(groupJson.getAsJsonPrimitive("ID")!!.asString),
            groupJson.getAsJsonPrimitive("GroupName")!!.asString,
            groupJson.getAsJsonPrimitive("PlayersMax")!!.asInt,
            groupJson.getAsJsonPrimitive("VirtualPlayers")!!.asInt,
            MatchMode.values()[groupJson.getAsJsonPrimitive("GameType")!!.asInt],
            Difficulty.values()[groupJson.getAsJsonPrimitive("Difficulty")!!.asInt],
            groupJson.getAsJsonPrimitive("Status")!!.asInt,
            UUID.fromString(groupJson.getAsJsonObject("Owner")!!.getAsJsonPrimitive("ID")!!.asString),
            jsonArrayToUUID(groupJson.getAsJsonArray("Players")!!)
        )
    }

    private fun jsonArrayToUUID(ids: JsonArray): ArrayList<UUID> {
        var arrayList = arrayListOf<UUID>()

        ids.forEach {
            arrayList.add(UUID.fromString(it.asJsonObject.getAsJsonPrimitive("ID").asString))
        }

        return arrayList
    }
}