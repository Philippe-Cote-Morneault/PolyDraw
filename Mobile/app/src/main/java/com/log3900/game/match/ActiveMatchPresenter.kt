package com.log3900.game.match

import android.os.Handler
import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.group.MatchMode
import com.log3900.settings.sound.SoundManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.DateFormatter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

abstract class ActiveMatchPresenter : Presenter {
    protected var activeMatchView: ActiveMatchView? = null
    var matchManager: MatchManager
    protected var lastShownTime: String? = null
    protected var canEnableGuessingView = false

    protected var leaveMatchHandler: Handler = Handler()
    public var matchEnded = false

    constructor(activeMatchView: ActiveMatchView, matchManager: MatchManager) {
        this.activeMatchView = activeMatchView
        this.matchManager = matchManager
        activeMatchView.setPlayers(matchManager.getCurrentMatch().players)
        subscribeToEvents()
    }

    fun guessPressed(text: String) {
        matchManager.makeGuess(text)
    }

    fun hintPressed() {
        matchManager.requestHint()
    }


    private fun onWordGuessedSucessfully() {
       // activeMatchView?.setPlayerStatus()
    }

    protected fun changeRemainingTime(remainingTime: Int) {
        val formattedTime = DateFormatter.formatDateToTime(Date(remainingTime.toLong()))
        if (lastShownTime == null || lastShownTime != formattedTime) {
            lastShownTime = formattedTime
            activeMatchView?.setTimeValue(formattedTime)

            if (remainingTime <= 10000) {
                activeMatchView?.pulseRemainingTime()
                SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_timer_warning)
            }
        }
    }

    protected open fun onMatchSynchronisation(synchronisation: Synchronisation) {
        val currentMatch = matchManager.getCurrentMatch()

        changeRemainingTime(synchronisation.time)
    }

    protected open fun onMatchPlayersUpdated() {
        activeMatchView?.notifyPlayersChanged()
    }

    protected open fun onGuessedWordRight(playerGuessedWord: PlayerGuessedWord) {
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_right)
        activeMatchView?.animateWordGuessedRight()
        activeMatchView?.enableGuessingView(false)
    }

    protected open fun onGuessedWordWrong() {
        activeMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.try_again))
        activeMatchView?.showCanvasMessageView(true)
        activeMatchView?.enableGuessingView(false)
        activeMatchView?.animateWordGuessedWrong()
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_wrong)
        Handler().postDelayed({
            activeMatchView?.showCanvasMessageView(false)
            if (canEnableGuessingView) {
                activeMatchView?.enableGuessingView(true)
            }
            activeMatchView?.clearGuessingViewText()
        }, 2000)

    }

    protected open fun onTimesUp(timesUp: TimesUp) {
        canEnableGuessingView = false
        activeMatchView?.enableGuessingView(false)

    }

    protected open fun onRoundEnded(roundEnded: RoundEnded) {
    }

    protected open fun onMatchEnded(matchEnded: MatchEnded) {
        this.matchEnded = true
        activeMatchView?.enableGuessingView(false)
        if (matchEnded.winner == AccountRepository.getInstance().getAccount().ID) {
            activeMatchView?.showConfetti()
        }
        leaveMatchHandler.postDelayed({
             activeMatchView?.quit()
            }, 5000
        )
    }

    protected open fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        activeMatchView?.clearCanvas()
        activeMatchView?.showWordGuessingView()
        activeMatchView?.enableGuessingView(true)
        canEnableGuessingView = true
        activeMatchView?.setWordToGuessLength(playerTurnToDraw.wordLength)
        activeMatchView?.enableDrawFunctions(false, playerTurnToDraw.drawingID, matchManager)
    }

    protected open fun onTeamateGuessedWordProperly(teamateGuessedWordProperly: TeamateGuessedWordProperly) {

    }

    protected open fun onTeamateGuessedWordInproperly(teamateGuessWordIncorrectly: TeamateGuessWordIncorrectly) {

    }


    private fun subscribeToEvents() {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.MATCH_SYNCHRONISATION -> onMatchSynchronisation(event.data as Synchronisation)
            EventType.MATCH_PLAYERS_UPDATED -> onMatchPlayersUpdated()
            EventType.GUESSED_WORD_RIGHT -> onGuessedWordRight(event.data as PlayerGuessedWord)
            EventType.GUESSED_WORD_WRONG -> onGuessedWordWrong()
            EventType.TIMES_UP -> onTimesUp(event.data as TimesUp)
            EventType.ROUND_ENDED -> onRoundEnded(event.data as RoundEnded)
            EventType.MATCH_ENDED -> onMatchEnded(event.data as MatchEnded)
            EventType.PLAYER_TURN_TO_DRAW -> onPlayerTurnToDraw(event.data as PlayerTurnToDraw)
            EventType.TEAMATE_GUESSED_WORD_PROPERLY -> onTeamateGuessedWordProperly(event.data as TeamateGuessedWordProperly)
            EventType.TEAMATE_GUESSED_WORD_INCORRECTLY -> onTeamateGuessedWordInproperly(event.data as TeamateGuessWordIncorrectly)
        }
    }

    protected open fun leaveMatch() {
        leaveMatchHandler.removeCallbacksAndMessages(null)
        MainApplication.instance.mainActivity?.closeChat()
        matchManager.leaveMatch()

    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        leaveMatch()
        EventBus.getDefault().unregister(this)
        activeMatchView = null
    }
}