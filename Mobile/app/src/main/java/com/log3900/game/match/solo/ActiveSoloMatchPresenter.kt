package com.log3900.game.match.solo

import android.os.Handler
import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.*
import com.log3900.settings.sound.SoundManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.account.AccountRepository
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

        activeSoloMatchView.setScore("Score: 0")
        activeSoloMatchView.setRemainingLives(soloMatchManager.getCurrentMatch().lives)
        activeSoloMatchView.showHintButton(true)

        val drawer = soloMatchManager.getCurrentMatch().players.find { it.isCPU }
        activeSoloMatchView.setDrawer(drawer!!)

        matchManager.notifyReadyToPlay()
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        var currentMatch = soloMatchManager.getCurrentMatch()
        currentMatch.lives = synchronisation.lives!!
        activeSoloMatchView?.setRemainingLives(synchronisation.lives!!)
        activeSoloMatchView?.setScore("Score: " + synchronisation.players[0].second)
    }

    override fun onGuessedWordWrong() {
        if (soloMatchManager.getCurrentMatch().lives > 1) {
            activeSoloMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.try_again))
            activeSoloMatchView?.showCanvasMessageView(true)
        }
        activeSoloMatchView?.enableGuessingView(false)
        activeSoloMatchView?.animateWordGuessedWrong()
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_wrong)
        Handler().postDelayed({
            activeSoloMatchView?.showCanvasMessageView(false)
            if (canEnableGuessingView) {
                activeSoloMatchView?.enableGuessingView(true)
            }
            activeSoloMatchView?.clearGuessingViewText()
        }, 2000)
    }

    override fun onTeamateGuessedWordProperly(teamateGuessedWordProperly: TeamateGuessedWordProperly) {
        super.onTeamateGuessedWordProperly(teamateGuessedWordProperly)
        activeSoloMatchView?.animateWordGuessedRight()
        activeSoloMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.you_guessed_correctly))
        activeSoloMatchView?.showCanvasMessageView(true)
    }

    override fun onRoundEnded(roundEnded: RoundEnded) {
        super.onRoundEnded(roundEnded)

        roundEnded.players.forEach {
            updatePlayerScore(it.userID, it.points, it.newPoints)
        }

        if (soloMatchManager.getCurrentMatch().lives == 0) {
            activeSoloMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.the_word_was, roundEnded.word))
            activeSoloMatchView?.showCanvasMessageView(true)
        }

        Handler().postDelayed({
            activeMatchView?.hideCanvas()
            activeMatchView?.hideRoundEndInfoView()
            activeSoloMatchView?.showCanvasMessageView(false)
            Handler().postDelayed({
                activeMatchView?.clearCanvas()
                activeMatchView?.showCanvas()
            }, 500)
        }, 2000)
    }

    override fun onMatchEnded(matchEnded: MatchEnded) {
        activeSoloMatchView?.showConfetti()
        val score = matchEnded.players.find { it.userID == AccountRepository.getInstance().getAccount().ID }!!.points
        activeSoloMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.solo_match_is_over_message, score))
        activeSoloMatchView?.showCanvasMessageView(true)
        Handler().postDelayed({
            activeSoloMatchView?.showCanvasMessageView(false)
        }, 2000)
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

    private fun updatePlayerScore(playerID: UUID, newScore: Int, variation: Int) {
        if (variation == 0) {
            return
        }

        var formattedScoreChange = "+"
        if (variation < 0) {
            formattedScoreChange = "-"
        }

        formattedScoreChange += variation

        activeSoloMatchView?.showScoreChangedAnimation(formattedScoreChange, variation > 0)
        activeSoloMatchView?.setScore("Score: " + newScore)
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