package com.log3900.utils.format.moshi

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import com.log3900.game.match.FFAMatch
import com.log3900.game.match.Match
import com.log3900.game.match.PlayerTurnToDraw
import com.log3900.game.match.TurnToDraw
import com.squareup.moshi.FromJson
import java.util.*

object MatchAdapter {
    @FromJson
    fun fromJson(matchJson: JsonObject): Match {
        val players = jsonArrayToPlayers(matchJson.getAsJsonArray("Players")!!)
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

    fun jsonArrayToPlayers(ids: JsonArray): ArrayList<Player> {
        var arrayList = arrayListOf<Player>()

        ids.forEach {
            arrayList.add(
                Player(
                    UUID.fromString(it.asJsonObject.getAsJsonPrimitive("UserID").asString),
                    it.asJsonObject.getAsJsonPrimitive("Username").asString,
                    it.asJsonObject.getAsJsonPrimitive("IsCPU").asBoolean
                )
            )
        }

        return arrayList
    }

    fun jsonToPlayerTurnToDraw(jsonObject: JsonObject): PlayerTurnToDraw {
        val userID = UUID.fromString(jsonObject.get("UserID").asString)
        val username = jsonObject.get("Username").asString
        val time = jsonObject.get("Time").asInt
        val drawingID = UUID.fromString(jsonObject.get("DrawingID").asString)
        val wordLength = jsonObject.get("Length").asInt

        return PlayerTurnToDraw(
            userID,
            username,
            time,
            drawingID,
            wordLength
        )
    }

    fun jsonToTurnToDraw(jsonObject: JsonObject): TurnToDraw {
        val word = jsonObject.get("Word").asString
        val time = jsonObject.get("Time").asInt
        val drawingID = UUID.fromString(jsonObject.get("DrawingID").asString)

        return TurnToDraw(
            word,
            time,
            drawingID
        )
    }
}