package com.log3900.game.match.coop

import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.*
import com.log3900.game.match.ffa.ActiveFFAMatchView
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.utils.format.DateFormatter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.math.absoluteValue

class ActiveCoopMatchPresenter : ActiveMatchPresenter {
    private var coopMatchManager: CoopMatchManager
    private var activeCoopMatchView: ActiveCoopMatchView? = null

    constructor(activeCoopMatchView: ActiveCoopMatchView) : super(activeCoopMatchView, CoopMatchManager()) {
        coopMatchManager = matchManager as CoopMatchManager
        this.activeCoopMatchView = activeMatchView as ActiveCoopMatchView

        activeCoopMatchView.setTeamScore((MainApplication.instance.getContext().resources.getString(R.string.team_score_title, 0)))
        activeCoopMatchView.setRemainingLives(coopMatchManager.getCurrentMatch().lives)
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

    private fun onCheckpoint(checkPoint: CheckPoint) {
        var formattedBonusTime = "+"

        if (checkPoint.bonus < 0) {
            formattedBonusTime = "-"
        }

        formattedBonusTime += DateFormatter.formatDateToTime(Date(checkPoint.bonus.toLong().absoluteValue))
        changeRemainingTime(checkPoint.totalTime)

        activeCoopMatchView?.showRemainingTimeChangedAnimation(formattedBonusTime, checkPoint.bonus > 0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.type) {
            EventType.CHECKPOINT -> onCheckpoint(event.data as CheckPoint)
        }
    }

    override fun destroy() {
        super.destroy()
        activeCoopMatchView = null
    }
}