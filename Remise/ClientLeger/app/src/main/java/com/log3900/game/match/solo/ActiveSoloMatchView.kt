package com.log3900.game.match.solo

import com.log3900.game.group.Player
import com.log3900.game.match.ActiveMatchView

interface ActiveSoloMatchView : ActiveMatchView {
    fun setScore(score: String)
    fun setRemainingLives(count: Int)
    fun removeRemainingLife()
    fun addRemainingLife()
    fun showScoreChangedAnimation(scoreChangedValue: String, isPositive: Boolean)
    fun setDrawer(player: Player)
    fun setBestScore(bestScore: String)
}