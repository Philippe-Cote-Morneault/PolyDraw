package com.log3900.game.match

import com.log3900.shared.architecture.Presenter

class ActiveMatchPresenter : Presenter {
    private var activeMatchView: ActiveMatchView? = null

    constructor(activeMatchView: ActiveMatchView) {
        this.activeMatchView = activeMatchView
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        activeMatchView = null
    }
}