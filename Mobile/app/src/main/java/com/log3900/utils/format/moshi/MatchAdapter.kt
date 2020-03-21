package com.log3900.utils.format.moshi

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import com.log3900.game.match.*
import com.squareup.moshi.FromJson
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

object MatchAdapter {
    @FromJson
    fun fromJson(matchJson: JsonObject): Match {
        val players = jsonArrayToPlayers(matchJson.getAsJsonArray("Players")!!)
        val matchType = MatchMode.values()[matchJson.get("GameType").asInt]
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

    fun jsonToPlayerGuessedWord(jsonObject: JsonObject): PlayerGuessedWord {
        Log.d("POTATO", "MatchAdapter parsing PlayerGuessedWord = ${jsonObject}")
        val username = jsonObject.get("Username").asString
        val userID = UUID.fromString(jsonObject.get("UserID").asString)
        val points = jsonObject.get("Points").asInt
        val pointsTotal = jsonObject.get("PointsTotal").asInt

        return PlayerGuessedWord(
            username,
            userID,
            points,
            pointsTotal
        )
    }

    fun jsonToSynchronisation(jsonObject: JsonObject): Synchronisation {
        val players = jsonObject.get("Players").asJsonArray
        val playerScores = jsonToSynchronisationPlayers(players)
        var laps: Int? = null
        var gameTime: Int? = null
        try {
            laps = jsonObject.get("Laps").asInt
        } catch (e: Exception) {
            laps = null
        }

        try {
            gameTime = jsonObject.get("GameTime").asInt
        } catch (e: Exception) {
            gameTime = null
        }

        val time = jsonObject.get("Time").asInt

        return Synchronisation(
            playerScores,
            laps,
            time,
            gameTime
        )
    }

    private fun jsonToSynchronisationPlayers(jsonArray: JsonArray): ArrayList<Pair<UUID, Int>> {
        val playerScores = arrayListOf<Pair<UUID, Int>>()
        jsonArray.forEach {
            val playerID = UUID.fromString(it.asJsonObject.get("UserID").asString)
            val score = it.asJsonObject.get("Points").asInt
            playerScores.add(Pair(playerID, score))
        }

        return playerScores
    }

    fun jsonToMatchEnded(jsonObject: JsonObject): MatchEnded {
        val players = jsonToPlayers(jsonObject.get("Players").asJsonArray)
        val winner = jsonObject.get("Winner").asString
        val time = jsonObject.get("Time").asInt

        return MatchEnded(players, winner, time)
    }

    private fun jsonToPlayers(jsonArray: JsonArray): ArrayList<com.log3900.game.match.Player> {
        val players = arrayListOf<com.log3900.game.match.Player>()
        jsonArray.forEach {
            val username = it.asJsonObject.get("Username").asString
            val userID = UUID.fromString(it.asJsonObject.get("UserID").asString)
            val points = it.asJsonObject.get("Points").asInt
            players.add(com.log3900.game.match.Player(username, userID, points))
        }

        return players
    }

    fun jsonToTimesUp(jsonObject: JsonObject): TimesUp {
        val type = TimesUp.Type.values()[jsonObject.get("Type").asInt]
        var word: String? = null
        
        if (jsonObject.has("Word")) {
            word = jsonObject.get("Word").asString
        }

        return TimesUp(type, word)
    }
}