package com.log3900.game.match.solo

import com.log3900.game.match.ActiveMatchPresenter
import com.log3900.game.match.coop.ActiveCoopMatchView

class ActiveSoloMatchPresenter : ActiveMatchPresenter {
    private var soloMatchManager: SoloMatchManager
    private var activeSoloMatchView: ActiveSoloMatchView? = null

    constructor(activeSoloMatchView: ActiveSoloMatchView) : super(activeSoloMatchView, SoloMatchManager()) {
        soloMatchManager = matchManager as SoloMatchManager
        this.activeSoloMatchView = activeMatchView as ActiveSoloMatchView
        activeSoloMatchView.enableHintButton(true)

        matchManager.notifyReadyToPlay()
    }

    override fun destroy() {
        super.destroy()
        activeSoloMatchView = null
    }
}