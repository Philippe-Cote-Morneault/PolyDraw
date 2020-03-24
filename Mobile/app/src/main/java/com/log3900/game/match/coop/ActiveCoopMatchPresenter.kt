package com.log3900.game.match.coop

import com.log3900.game.match.ActiveMatchPresenter
import com.log3900.game.match.ffa.ActiveFFAMatchView

class ActiveCoopMatchPresenter : ActiveMatchPresenter {
    private var coopMatchManager: CoopMatchManager
    private var activeCoopMatchView: ActiveCoopMatchView? = null

    constructor(activeCoopMatchView: ActiveCoopMatchView) : super(activeCoopMatchView, CoopMatchManager()) {
        coopMatchManager = matchManager as CoopMatchManager
        this.activeCoopMatchView = activeMatchView as ActiveCoopMatchView
    }

    override fun destroy() {
        super.destroy()
        activeCoopMatchView = null
    }
}