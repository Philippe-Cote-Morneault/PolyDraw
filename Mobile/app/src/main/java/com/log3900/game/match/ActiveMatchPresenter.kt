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
    protected var matchManager: MatchManager
    protected var lastShownTime: String? = null

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

    private fun onMatchPlayersUpdated() {
        activeMatchView?.notifyPlayersChanged()
    }

    protected open fun onGuessedWordRight(playerGuessedWord: PlayerGuessedWord) {
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_right)
        activeMatchView?.animateWordGuessedRight()
    }

    protected open fun onGuessedWordWrong() {
        activeMatchView?.animateWordGuessedWrong()
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_wrong)
    }

    private fun onTimesUp(timesUp: TimesUp) {
        if (timesUp.type == TimesUp.Type.WORD_END) {
            Handler().postDelayed({
                activeMatchView?.hideCanvas()
                activeMatchView?.hideRoundEndInfoView()
                Handler().postDelayed({
                    activeMatchView?.clearCanvas()
                    activeMatchView?.showCanvas()
                }, 1500)
            }, 2000)
        }
    }

    private fun onRoundEnded(roundEnded: RoundEnded) {
        val playerScores: ArrayList<Pair<String, Int>> = arrayListOf()
        roundEnded.players.forEach {
            playerScores.add(Pair(it.username, it.newPoints))
        }
        activeMatchView?.showRoundEndInfoView(roundEnded.word, playerScores)
    }

    private fun onMatchEnded(matchEnded: MatchEnded) {
        if (matchEnded.winner == AccountRepository.getInstance().getAccount().ID) {
            activeMatchView?.showConfetti()
        }
        val playerScores: ArrayList<Pair<String, Int>> = arrayListOf()
        matchEnded.players.forEach {
            playerScores.add(Pair(it.username, it.points))
        }
        activeMatchView?.showMatchEndInfoView(matchEnded.winnerName, playerScores)
    }

    protected open fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        activeMatchView?.clearCanvas()
        activeMatchView?.showWordGuessingView()
        activeMatchView?.setWordToGuessLength(playerTurnToDraw.wordLength)
        activeMatchView?.enableDrawFunctions(false, playerTurnToDraw.drawingID)
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
        }
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        matchManager.leaveMatch()
        EventBus.getDefault().unregister(this)
        activeMatchView = null
    }
}