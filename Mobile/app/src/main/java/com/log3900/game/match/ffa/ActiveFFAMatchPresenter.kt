package com.log3900.game.match.ffa

import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.ActiveMatchPresenter
import com.log3900.game.match.ActiveMatchView
import com.log3900.game.match.MatchManager
import com.log3900.game.match.Synchronisation
import com.log3900.shared.architecture.Presenter

class ActiveFFAMatchPresenter : ActiveMatchPresenter {
    private var FFAMatchManager: FFAMatchManager
    private var activeFFAMatchView: ActiveFFAMatchView? = null

    constructor(activeFFAMatchView: ActiveFFAMatchView) : super(activeFFAMatchView, FFAMatchManager()) {
        FFAMatchManager = matchManager as FFAMatchManager
        this.activeFFAMatchView = activeMatchView as ActiveFFAMatchView
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        val totalRounds = FFAMatchManager.getCurrentMatch().laps
        activeFFAMatchView?.setTurnsValue(MainApplication.instance.getString(R.string.turn) + " ${synchronisation.laps}/${totalRounds}")
    }

    override fun destroy() {
        super.destroy()
        activeFFAMatchView = null
    }
}