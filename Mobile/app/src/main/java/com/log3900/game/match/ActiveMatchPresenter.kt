package com.log3900.game.match

import com.log3900.shared.architecture.Presenter

class ActiveMatchPresenter : Presenter {
    private var activeMatchView: ActiveMatchView? = null

    constructor(activeMatchView: ActiveMatchView) {
        this.activeMatchView = activeMatchView
    }

    override fun resume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}