package com.log3900.game.match

import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

abstract class Match(
    var players: ArrayList<Player>,
    var matchType: MatchMode,
    var time: Int,
    var lives: Int
)

class FFAMatch(
    players: ArrayList<Player>,
    matchType: MatchMode,
    time: Int,
    lives: Int,
    var laps: Int
) : Match(
    players,
    matchType,
    time,
    lives
)

class CoopMatch(
    players: ArrayList<Player>,
    matchType: MatchMode,
    time: Int,
    lives: Int
) : Match(
    players,
    matchType,
    time,
    lives
)

class SoloMatch(
    players: ArrayList<Player>,
    matchType: MatchMode,
    time: Int,
    lives: Int
) : Match(
    players,
    matchType,
    time,
    lives
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
    var lapTotal: Int?,
    var lives: Int?
)

class MatchEnded(
    var players: ArrayList<com.log3900.game.match.Player>,
    var winner: UUID,
    var winnerName: String,
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

class RoundEnded(
    var players: ArrayList<Player>,
    var word: String
) {
    class Player(
        var userID: UUID,
        var username: String,
        var isCPU: Boolean,
        var points: Int,
        var newPoints: Int
    )
}

class HintResponse(
    var userID: UUID?,
    var hint: String?,
    var hintsLeft: Int?,
    var error: String?
)

class CheckPoint(
    var totalTime: Int,
    var bonus: Int
)