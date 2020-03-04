package com.log3900.game.waiting_room

import com.log3900.shared.architecture.Presenter

class MatchWaitingRoomPresenter : Presenter {
    private var matchWaitingRoomView: MatchWaitingRoomView

    constructor(matchWaitingRoomView: MatchWaitingRoomView) {
        this.matchWaitingRoomView = matchWaitingRoomView
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
    }
}