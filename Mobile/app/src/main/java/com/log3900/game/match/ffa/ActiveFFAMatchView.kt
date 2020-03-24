package com.log3900.game.match.ffa

import com.log3900.game.match.ActiveMatchView

interface ActiveFFAMatchView : ActiveMatchView {
    fun setTurnsValue(turns: String)
}