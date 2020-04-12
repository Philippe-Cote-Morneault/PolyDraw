package com.log3900.game.match.coop

import android.os.Handler
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

class ActiveCoopMatchPresenter : ActiveMatchPresenter {
    private var coopMatchManager: CoopMatchManager
    private var activeCoopMatchView: ActiveCoopMatchView? = null

    constructor(activeCoopMatchView: ActiveCoopMatchView) : super(activeCoopMatchView, CoopMatchManager()) {
        coopMatchManager = matchManager as CoopMatchManager
        this.activeCoopMatchView = activeMatchView as ActiveCoopMatchView

        activeCoopMatchView.setTeamScore((MainApplication.instance.getContext().resources.getString(R.string.team_score_title, 0)))
        activeCoopMatchView.setRemainingLives(coopMatchManager.getCurrentMatch().lives)
        activeCoopMatchView.showHintButton(true)

        val drawer = coopMatchManager.getCurrentMatch().players.find { it.isCPU }
        activeCoopMatchView.setDrawer(drawer!!)

        matchManager.notifyReadyToPlay()
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        var currentMatch = coopMatchManager.getCurrentMatch()
        currentMatch.lives = synchronisation.lives!!
        activeCoopMatchView?.setRemainingLives(synchronisation.lives!!)
        activeCoopMatchView?.setTeamScore(MainApplication.instance.getContext().resources.getString(R.string.team_score_title,synchronisation.players[0].second))
    }

    override fun onRoundEnded(roundEnded: RoundEnded) {
        super.onRoundEnded(roundEnded)

        roundEnded.players.forEach {
            updatePlayerScore(it.userID, it.points, it.newPoints)
        }

        if (coopMatchManager.getCurrentMatch().lives == 0) {
            activeCoopMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.the_word_was, roundEnded.word))
            activeCoopMatchView?.showCanvasMessageView(true)
        }

        activeCoopMatchView?.setTeamScore(MainApplication.instance.getContext().resources.getString(R.string.team_score_title, roundEnded.players[0].points))

        Handler().postDelayed({
            if (!matchEnded) {
                activeCoopMatchView?.hideCanvas()
                activeCoopMatchView?.hideRoundEndInfoView()
                activeCoopMatchView?.showCanvasMessageView(false)
                Handler().postDelayed({
                    activeCoopMatchView?.clearCanvas()
                    activeCoopMatchView?.showCanvas()
                }, 500)
            }
        }, 3000)
    }

    override fun onTeamateGuessedWordProperly(teamateGuessedWordProperly: TeamateGuessedWordProperly) {
        super.onTeamateGuessedWordProperly(teamateGuessedWordProperly)
        if (teamateGuessedWordProperly.userID == AccountRepository.getInstance().getAccount().ID) {
            activeCoopMatchView?.animateWordGuessedRight()
            activeCoopMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.you_guessed_correctly))
            activeCoopMatchView?.showCanvasMessageView(true)
        } else {
            activeCoopMatchView?.enableGuessingView(false)
            activeCoopMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.coop_teamate_guessed_correctly, teamateGuessedWordProperly.username, teamateGuessedWordProperly.word))
            activeCoopMatchView?.showCanvasMessageView(true)
        }
    }

    override fun onTeamateGuessedWordInproperly(teamateGuessWordIncorrectly: TeamateGuessWordIncorrectly) {
        super.onTeamateGuessedWordInproperly(teamateGuessWordIncorrectly)
        if (teamateGuessWordIncorrectly.userID != AccountRepository.getInstance().getAccount().ID) {
            activeCoopMatchView?.animateBadTeamateGuessWarning()
        }
    }

    override fun onGuessedWordWrong() {
        if (coopMatchManager.getCurrentMatch().lives > 1) {
            activeCoopMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.try_again))
            activeCoopMatchView?.showCanvasMessageView(true)
        }
        activeCoopMatchView?.enableGuessingView(false)
        activeCoopMatchView?.animateWordGuessedWrong()
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_wrong)
        Handler().postDelayed({
            activeCoopMatchView?.showCanvasMessageView(false)
            if (canEnableGuessingView) {
                activeCoopMatchView?.enableGuessingView(true)
            }
            activeCoopMatchView?.clearGuessingViewText()
        }, 2000)
    }


    override fun onMatchEnded(matchEnded: MatchEnded) {
        this.matchEnded = true
        activeCoopMatchView?.showConfetti()
        val score = matchEnded.players.find { it.userID == AccountRepository.getInstance().getAccount().ID }!!.points
        activeCoopMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.solo_match_is_over_message, score))
        activeCoopMatchView?.showCanvasMessageView(true)
        Handler().postDelayed({
            activeCoopMatchView?.showCanvasMessageView(false)
        }, 5000)
        leaveMatchHandler.postDelayed({
            activeMatchView?.quit()
        }, 5000)
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

    private fun updatePlayerScore(playerID: UUID, newScore: Int, variation: Int) {
        if (variation == 0) {
            return
        }

        var formattedScoreChange = "+"
        if (variation < 0) {
            formattedScoreChange = "-"
        }

        formattedScoreChange += variation

        activeCoopMatchView?.showScoreChangedAnimation(formattedScoreChange, variation > 0)
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