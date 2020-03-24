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
    private var lastShownTime: String? = null

    constructor(activeMatchView: ActiveMatchView, matchManager: MatchManager) {
        this.activeMatchView = activeMatchView
        this.matchManager = matchManager
        activeMatchView.setPlayers(matchManager.getCurrentMatch().players)
        activeMatchView.setPlayerScores(matchManager.getPlayerScores())
        subscribeToEvents()
        matchManager.notifyReadyToPlay()
    }

    fun guessPressed(text: String) {
        matchManager.makeGuess(text)
    }

    private fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        activeMatchView?.clearCanvas()
        activeMatchView?.clearAllPlayerStatusRes()
        activeMatchView?.setPlayerStatus(playerTurnToDraw.userID, R.drawable.ic_edit_black)
        activeMatchView?.showWordGuessingView()
        activeMatchView?.setWordToGuessLength(playerTurnToDraw.wordLength)
        activeMatchView?.enableDrawFunctions(false, playerTurnToDraw.drawingID)
    }

    private fun onWordGuessedSucessfully() {
       // activeMatchView?.setPlayerStatus()
    }

    private fun onPlayerGuessedWord(playerGuessedWord: PlayerGuessedWord) {
        activeMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)
    }


    private fun onTurnToDraw(turnToDraw: TurnToDraw) {
        activeMatchView?.clearCanvas()
        activeMatchView?.clearAllPlayerStatusRes()
        activeMatchView?.setPlayerStatus(AccountRepository.getInstance().getAccount().ID, R.drawable.ic_edit_black)
        activeMatchView?.showWordToDrawView()
        activeMatchView?.setWordToDraw(turnToDraw.word)
        activeMatchView?.enableDrawFunctions(true, turnToDraw.drawingID)
    }

    protected open fun onMatchSynchronisation(synchronisation: Synchronisation) {
        val currentMatch = matchManager.getCurrentMatch()
        val formattedTime = DateFormatter.formatDateToTime(Date(synchronisation.time.toLong()))

        if (lastShownTime == null || lastShownTime != formattedTime) {
            lastShownTime = formattedTime
            activeMatchView?.setTimeValue(formattedTime)
        }

        if (synchronisation.time <= 10000) {
            activeMatchView?.pulseRemainingTime()
            SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_timer_warning)
        }
    }

    private fun onMatchPlayersUpdated() {
        activeMatchView?.notifyPlayersChanged()
    }

    private fun onGuessedWordRight(playerGuessedWord: PlayerGuessedWord) {
        activeMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_right)
        activeMatchView?.animateWordGuessedRight()
    }

    private fun onGuessedWordWrong() {
        activeMatchView?.animateWordGuessedWrong()
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_wrong)
    }

    private fun onTimesUp(timesUp: TimesUp) {
        if (timesUp.type == TimesUp.Type.WORD_END) {
            Handler().postDelayed({
                activeMatchView?.hideCanvas()
                activeMatchView?.hideRoundEndInfoView()
                Handler().postDelayed({
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


    private fun subscribeToEvents() {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.PLAYER_TURN_TO_DRAW -> onPlayerTurnToDraw(event.data as PlayerTurnToDraw)
            EventType.TURN_TO_DRAW -> onTurnToDraw(event.data as TurnToDraw)
            EventType.PLAYER_GUESSED_WORD -> onPlayerGuessedWord(event.data as PlayerGuessedWord)
            EventType.MATCH_SYNCHRONISATION -> onMatchSynchronisation(event.data as Synchronisation)
            EventType.MATCH_PLAYERS_UPDATED -> onMatchPlayersUpdated()
            EventType.GUESSED_WORD_RIGHT -> onGuessedWordRight(event.data as PlayerGuessedWord)
            EventType.GUESSED_WORD_WRONG -> onGuessedWordWrong()
            EventType.TIMES_UP -> onTimesUp(event.data as TimesUp)
            EventType.ROUND_ENDED -> onRoundEnded(event.data as RoundEnded)
            EventType.MATCH_ENDED -> onMatchEnded(event.data as MatchEnded)
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