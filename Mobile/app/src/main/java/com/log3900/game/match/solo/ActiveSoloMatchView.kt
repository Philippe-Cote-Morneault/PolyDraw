package com.log3900.game.match.solo

import com.log3900.game.match.ActiveMatchView

interface ActiveSoloMatchView : ActiveMatchView {
    fun setRemainingLives(count: Int)
    fun removeRemainingLife()
    fun addRemainingLife()
}