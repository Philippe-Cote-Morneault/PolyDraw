package com.log3900.profile.stats

import java.util.*

data class UserStats(
    val gamesPlayed:            Int,
    val winRation:              Double,
    val averageGameDuration:    Int,
    val timePlayed:             Int,
    val connectionHistory:      List<Connection>,
    val gamesPlayedHistory:     List<GamePlayed>,
    val achievements:           List<Achievement>
)

data class Connection(
    val connectionTime:     Date,
    val disconnectionTime:  Date
)

data class GamePlayed(
    val winner: String,
    val date:   Date,
    val result: String  // TODO: Confirm
)

data class Achievement(
    val title:          String,
    val description:    String,
    val unlockDate:     Date
)