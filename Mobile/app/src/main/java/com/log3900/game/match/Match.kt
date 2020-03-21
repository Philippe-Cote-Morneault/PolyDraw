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

class TurnToDraw(
    var word: String,
    var time: Int,
    var drawingID: UUID
)

class PlayerGuessedWord(
    var username: String,
    var userID: UUID,
    var points: Int,
    var pointsTotal: Int
)

class Synchronisation(
    var players: ArrayList<Pair<UUID, Int>>,
    var laps: Int?,
    var time: Int,
    var gameTime: Int?
)

class MatchEnded(
    var players: ArrayList<com.log3900.game.match.Player>,
    var winner: String,
    var time: Int
)

class Player(
    var username: String,
    var userID: UUID,
    var points: Int
)

class TimesUp(
    var type: Type,
    var word: String?
) {
    enum class Type {
        WORD_END,
        MATCH_END
    }
}