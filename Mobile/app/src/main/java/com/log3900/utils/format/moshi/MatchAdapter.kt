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
        Log.d("POTATO", "Convertin match $matchJson")
        val players = jsonArrayToPlayers(matchJson.getAsJsonArray("Players")!!)
        val matchType = MatchMode.values()[matchJson.get("GameType").asInt]
        val timeImage = matchJson.get("TimeImage").asInt
        val lives = matchJson.get("Lives").asInt

        when (matchType) {
            MatchMode.FFA -> {
                return FFAMatch(
                    players,
                    matchType,
                    timeImage,
                    matchJson.get("Laps").asInt,
                    lives
                )
            }
            MatchMode.COOP -> {
                return CoopMatch(
                    players,
                    matchType,
                    timeImage,
                    lives
                )
            }
            MatchMode.SOLO -> {
                return SoloMatch(
                    players,
                    matchType,
                    timeImage,
                    lives
                )
            }
        }

        return FFAMatch(
            players,
            matchType,
            timeImage,
            0,
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
        val pointsTotal = jsonObject.get("Points").asInt
        val points = jsonObject.get("NewPoints").asInt

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
        try {
            laps = jsonObject.get("Laps").asInt
        } catch (e: Exception) {
            laps = null
        }

        var lapTotal: Int? = null

        if (jsonObject.has("LapTotal")) {
            lapTotal = jsonObject.get("LapTotal").asInt
        }

        var lives: Int? = null

        if (jsonObject.has("Lives")) {
            lives = jsonObject.get("Lives").asInt
        }

        val time = jsonObject.get("Time").asInt

        return Synchronisation(
            playerScores,
            laps,
            time,
            lapTotal,
            lives
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
        val winner = UUID.fromString(jsonObject.get("Winner").asString)
        val winnerName = jsonObject.get("WinnerName").asString
        val time = jsonObject.get("Time").asInt

        return MatchEnded(players, winner, winnerName, time)
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
        val type = TimesUp.Type.values()[jsonObject.get("Type").asInt - 1]
        var word: String? = null

        if (jsonObject.has("Word")) {
            word = jsonObject.get("Word").asString
        }

        return TimesUp(type, word)
    }

    fun jsonToRoundEnded(jsonObject: JsonObject): RoundEnded {
        val word = jsonObject.get("Word").asString
        val players = jsonToRoundEndedPlayers(jsonObject.get("Players").asJsonArray)

        return RoundEnded(players, word)
    }

    private fun jsonToRoundEndedPlayers(jsonArray: JsonArray): ArrayList<RoundEnded.Player> {
        val players: ArrayList<RoundEnded.Player> = arrayListOf()

        jsonArray.forEach {
            val userID = UUID.fromString(it.asJsonObject.get("UserID").asString)
            val username = it.asJsonObject.get("Username").asString
            val isCPU = it.asJsonObject.get("IsCPU").asBoolean
            val points = it.asJsonObject.get("Points").asInt
            val newPoints = it.asJsonObject.get("NewPoints").asInt

            players.add(RoundEnded.Player(userID, username, isCPU, points, newPoints))
        }

        return players
    }

    fun jsonToHintResponse(jsonObject: JsonObject): HintResponse {
        var hint: String? = null
        var error: String? = null
        var userID: UUID? = null
        var hintsLeft: Int? = null

        if (jsonObject.has("Hint")) {
            hint = jsonObject.get("Hint").asString
        }

        if (jsonObject.has("Error")) {
            error = jsonObject.get("Error").asString
        }

        if (jsonObject.has("UserID")) {
            userID = UUID.fromString(jsonObject.get("UserID").asString)
        }

        if (jsonObject.has("HintsLeft")) {
            hintsLeft = jsonObject.get("HintsLeft").asInt
        }

        return HintResponse(userID, hint, hintsLeft, error)
    }

    fun jsonToCheckpoint(jsonObject: JsonObject): CheckPoint {
        val totalTime = jsonObject.get("TotalTime").asInt
        val bonus = jsonObject.get("Bonus").asInt

        return CheckPoint(totalTime, bonus)
    }
}