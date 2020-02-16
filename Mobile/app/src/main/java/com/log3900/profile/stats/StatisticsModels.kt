package com.log3900.profile.stats

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

// TODO: use Dates?

data class UserStats(
    @Json(name = "Stats")                   val generalStats:           GeneralStats,
    @Json(name = "ConnectionHistory")       val connectionHistory:      List<Connection>,
    @Json(name = "MatchesPlayedHistory")    val gamesPlayedHistory:     List<GamePlayed>,
    @Json(name = "Achievements")            val achievements:           List<Achievement>
)

data class GeneralStats(
    @Json(name = "GamesPlayed")             val gamesPlayed:            Int,
    @Json(name = "WinRatio")                val winRation:              Double,
    @Json(name = "AvgGameDuration")         val averageGameDuration:    Int,
    @Json(name = "TimePlayed")              val timePlayed:             Int
)

data class Connection(
    @Json(name = "ConnectedAt")     val connectedAt:    Int,
    @Json(name = "DeconnectedAt")   val deconnectedAt:  Int
)

data class GamePlayed(
    @Json(name = "MatchDuration")   val duration:       Int,
    @Json(name = "WinnerName")      val winner:         String,
    @Json(name = "MatchType")       val matchType:      String,
    @Json(name = "PlayersNames")    val playerNames:    List<PlayerName>
)

data class PlayerName(
    @Json(name = "PlayerName") val name: String
)

data class Achievement(
    @Json(name = "TropheeName")     val title:          String,
    @Json(name = "Description")     val description:    String,
    @Json(name = "ObtainingDate")   val unlockDate:     Int
)