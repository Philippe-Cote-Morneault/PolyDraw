package com.log3900.game.match

import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

interface ActiveMatchView {
    fun setPlayers(players: ArrayList<Player>)
    fun setPlayerStatus(playerID: UUID, statusImageRes: Int)
    fun clearAllPlayerStatusRes()
    fun setWordToGuessLength(length: Int)
    fun setWordToDraw(word: String)
    fun enableDrawFunctions(enable: Boolean, drawingID: UUID?)
    fun clearCanvas()
    fun setTimeValue(time: String)
    fun showWordGuessingView()
    fun showWordToDrawView()
}