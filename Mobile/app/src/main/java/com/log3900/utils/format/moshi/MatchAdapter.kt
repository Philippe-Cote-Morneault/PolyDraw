package com.log3900.utils.format.moshi

import android.util.Log
import com.google.gson.JsonObject
import com.log3900.game.group.MatchMode
import com.log3900.game.match.FFAMatch
import com.log3900.game.match.Match
import com.squareup.moshi.FromJson

object MatchAdapter {
    @FromJson
    fun fromJson(matchJson: JsonObject): Match {
        Log.d("POTATO", "Converting match = $matchJson")
        val players = GroupAdapter().jsonArrayToPlayers(matchJson.getAsJsonArray("Players")!!)
        val matchType = MatchMode.values()[matchJson.get("GameType").asInt + 1]
        val timeImage = matchJson.get("TimeImage").asInt

        when (matchType) {
            MatchMode.FFA -> {
                return FFAMatch(
                    players,
                    matchType,
                    timeImage,
                    matchJson.get("Laps").asInt
                )
            }
        }

        return FFAMatch(
            players,
            matchType,
            timeImage,
            0
        )
    }
}