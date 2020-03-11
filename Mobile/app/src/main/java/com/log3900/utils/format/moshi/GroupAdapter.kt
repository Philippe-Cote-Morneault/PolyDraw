package com.log3900.utils.format.moshi

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.game.group.Difficulty
import com.log3900.game.group.Group
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
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
        return Group(
            UUID.fromString(groupJson.getAsJsonPrimitive("ID")!!.asString),
            groupJson.getAsJsonPrimitive("GroupName")!!.asString,
            groupJson.getAsJsonPrimitive("PlayersMax")!!.asInt,
            matchType,
            Difficulty.values()[groupJson.getAsJsonPrimitive("Difficulty")!!.asInt],
            UUID.fromString(groupJson.getAsJsonPrimitive("OwnerID").asString),
            groupJson.getAsJsonPrimitive("OwnerName").asString,
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