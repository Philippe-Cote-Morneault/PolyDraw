package com.log3900.game.lobby

import com.log3900.shared.architecture.Presenter

class MatchLobbyPresenter : Presenter {
    private var matchLobbyView: MatchLobbyView? = null

    constructor(matchLobbyView: MatchLobbyView) {
        this.matchLobbyView = matchLobbyView
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        matchLobbyView = null
    }
}