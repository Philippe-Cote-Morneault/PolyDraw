package com.log3900.game.match.ffa

import com.log3900.game.match.ActiveMatchView
import java.util.*
import kotlin.collections.HashMap

interface ActiveFFAMatchView : ActiveMatchView {
    fun setTurnsValue(turns: String)
    fun setPlayerStatus(playerID: UUID, statusImageRes: Int)
    fun setPlayerScores(playerScores: HashMap<UUID, Int>)
    fun clearAllPlayerStatusRes()
}