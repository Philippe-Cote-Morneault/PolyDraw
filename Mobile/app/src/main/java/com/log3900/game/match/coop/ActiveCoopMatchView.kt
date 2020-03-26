package com.log3900.game.match.coop

import com.log3900.game.match.ActiveMatchView

interface ActiveCoopMatchView : ActiveMatchView {
    fun setTeamScore(score: String)
    fun setRemainingLives(count: Int)
    fun removeRemainingLife()
    fun addRemainingLife()
}