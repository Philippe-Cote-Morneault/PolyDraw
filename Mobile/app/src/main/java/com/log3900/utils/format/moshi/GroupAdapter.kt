package com.log3900.utils.format.moshi

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.game.group.*
import com.squareup.moshi.FromJson
import java.util.*

class GroupAdapter {
    @FromJson
    fun fromJson(groupJson: JsonObject): Group {
        Log.d("POTATO", "Converting $groupJson")
        var matchType: MatchMode? = null
        if (groupJson.has("Mode")) {
            matchType = MatchMode.values()[groupJson.get("Mode").asInt]
        } else {
            matchType = MatchMode.values()[groupJson.get("GameType").asInt]
        }

        var language: Language? = null

        if (groupJson.has("Language")) {
            language = Language.values()[groupJson.get("Language").asInt]
        } else {
            language = Language.ENGLISH
        }

        return Group(
            UUID.fromString(groupJson.getAsJsonPrimitive("ID")!!.asString),
            groupJson.getAsJsonPrimitive("GroupName")!!.asString,
            groupJson.getAsJsonPrimitive("PlayersMax")!!.asInt,
            matchType,
            Difficulty.values()[groupJson.getAsJsonPrimitive("Difficulty")!!.asInt],
            UUID.fromString(groupJson.getAsJsonPrimitive("OwnerID").asString),
            groupJson.getAsJsonPrimitive("OwnerName").asString,
            language,
            jsonArrayToPlayers(groupJson.getAsJsonArray("Players")!!)
        )
    }

    fun jsonArrayToPlayers(ids: JsonArray): ArrayList<Player> {
        var arrayList = arrayListOf<Player>()

        ids.forEach {
            arrayList.add(Player(
                UUID.fromString(it.asJsonObject.getAsJsonPrimitive("ID").asString),
                it.asJsonObject.getAsJsonPrimitive("Username").asString,
                it.asJsonObject.getAsJsonPrimitive("IsCPU").asBoolean
            ))
        }

        return arrayList
    }
}