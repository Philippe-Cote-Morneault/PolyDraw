package com.log3900.game.match.coop

import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.ActiveMatchPresenter
import com.log3900.game.match.Synchronisation
import com.log3900.game.match.ffa.ActiveFFAMatchView

class ActiveCoopMatchPresenter : ActiveMatchPresenter {
    private var coopMatchManager: CoopMatchManager
    private var activeCoopMatchView: ActiveCoopMatchView? = null

    constructor(activeCoopMatchView: ActiveCoopMatchView) : super(activeCoopMatchView, CoopMatchManager()) {
        coopMatchManager = matchManager as CoopMatchManager
        this.activeCoopMatchView = activeMatchView as ActiveCoopMatchView

        activeCoopMatchView.setTeamScore((MainApplication.instance.getContext().resources.getString(R.string.team_score_title, 0)))
        activeCoopMatchView.setRemainingLives(3)
        activeCoopMatchView.enableHintButton(true)

        matchManager.notifyReadyToPlay()
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        activeCoopMatchView?.setRemainingLives(synchronisation.lives!!)
        activeCoopMatchView?.setTeamScore(MainApplication.instance.getContext().resources.getString(R.string.team_score_title,synchronisation.players[0].second))
    }

    override fun onGuessedWordWrong() {
        super.onGuessedWordWrong()
    }

    override fun destroy() {
        super.destroy()
        activeCoopMatchView = null
    }
}