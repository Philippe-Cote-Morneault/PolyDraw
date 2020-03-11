package com.log3900.game.match

import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

abstract class Match(
    var players: ArrayList<Player>,
    var matchType: MatchMode,
    var timeImage: Int
)

class FFAMatch(
    players: ArrayList<Player>,
    matchType: MatchMode,
    timeImage: Int,
    var laps: Int
) : Match(
    players,
    matchType,
    timeImage
)

class PlayerTurnToDraw(
    var userID: UUID,
    var username: String,
    var time: Int,
    var drawingID: UUID,
    var wordLength: Int
)