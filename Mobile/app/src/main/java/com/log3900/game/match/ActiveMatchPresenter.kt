package com.log3900.game.match

import com.log3900.shared.architecture.Presenter

class ActiveMatchPresenter : Presenter {
    private var activeMatchView: ActiveMatchView? = null
    private var matchManager: MatchManager

    constructor(activeMatchView: ActiveMatchView) {
        this.activeMatchView = activeMatchView
        this.matchManager = MatchManager()
        activeMatchView.setPlayers(matchManager.getCurrentMatch().players)
        matchManager.notifyReadyToPlay()
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        activeMatchView = null
    }
}