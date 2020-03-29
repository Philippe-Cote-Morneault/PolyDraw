package com.log3900.game.match.solo

import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.ActiveMatchPresenter
import com.log3900.game.match.CheckPoint
import com.log3900.game.match.Synchronisation
import com.log3900.game.match.coop.ActiveCoopMatchView
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.utils.format.DateFormatter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.math.absoluteValue

class ActiveSoloMatchPresenter : ActiveMatchPresenter {
    private var soloMatchManager: SoloMatchManager
    private var activeSoloMatchView: ActiveSoloMatchView? = null

    constructor(activeSoloMatchView: ActiveSoloMatchView) : super(activeSoloMatchView, SoloMatchManager()) {
        soloMatchManager = matchManager as SoloMatchManager
        this.activeSoloMatchView = activeMatchView as ActiveSoloMatchView

        activeSoloMatchView.setRemainingLives(3)
        activeSoloMatchView.enableHintButton(true)

        matchManager.notifyReadyToPlay()
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        activeSoloMatchView?.setRemainingLives(synchronisation.lives!!)
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

        activeSoloMatchView?.showRemainingTimeChangedAnimation(formattedBonusTime, checkPoint.bonus > 0)
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
        activeSoloMatchView = null
    }
}