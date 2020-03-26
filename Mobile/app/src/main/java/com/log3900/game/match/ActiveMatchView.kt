package com.log3900.game.match

import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface ActiveMatchView {
    fun setPlayers(players: ArrayList<Player>)
    fun setWordToGuessLength(length: Int)
    fun setWordToDraw(word: String)
    fun enableDrawFunctions(enable: Boolean, drawingID: UUID?)
    fun clearCanvas()
    fun setTimeValue(time: String)
    fun showWordGuessingView()
    fun showWordToDrawView()
    fun notifyPlayersChanged()
    fun showCanvas()
    fun hideCanvas()
    fun showConfetti()
    fun pulseRemainingTime()
    fun animateWordGuessedWrong()
    fun animateWordGuessedRight()
    fun showRoundEndInfoView(word: String, players: ArrayList<Pair<String, Int>>)
    fun hideRoundEndInfoView()
    fun showMatchEndInfoView(winnerName: String, players: ArrayList<Pair<String, Int>>)
    fun hideMatchEndInfoView()
    fun enableHintButton(enable: Boolean)
}