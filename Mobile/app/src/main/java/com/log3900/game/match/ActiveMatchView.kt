package com.log3900.game.match

import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface ActiveMatchView {
    fun setPlayers(players: ArrayList<Player>)
    fun setWordToGuessLength(length: Int)
    fun setWordToDraw(word: String)
    fun enableDrawFunctions(enable: Boolean, drawingID: UUID?, matchManager: MatchManager? = null)
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
    fun showMatchEndInfoView(winnerName: String, players: ArrayList<Pair<String, Int>>, isUser: Boolean)
    fun hideMatchEndInfoView()
    fun showHintButton(enable: Boolean)
    fun showRemainingTimeChangedAnimation(timeChangedValue: String, isPositive: Boolean)
    fun setCanvasMessage(message: String)
    fun showCanvasMessageView(show: Boolean)
    fun onBackButtonPressed()
    fun enableGuessingView(enable: Boolean)
    fun clearGuessingViewText()
}