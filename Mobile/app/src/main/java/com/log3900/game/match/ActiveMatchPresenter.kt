package com.log3900.game.match

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

class ActiveMatchPresenter : Presenter {
    private var activeMatchView: ActiveMatchView? = null
    private var matchManager: MatchManager

    constructor(activeMatchView: ActiveMatchView) {
        this.activeMatchView = activeMatchView
        this.matchManager = MatchManager()
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
        activeMatchView?.showCanvas()
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
        activeMatchView?.showCanvas()
    }

    private fun onMatchSynchronisation(synchronisation: Synchronisation) {
        val currentMatch = matchManager.getCurrentMatch()
        activeMatchView?.setTimeValue(DateFormatter.formatDateToTime(Date(synchronisation.time.toLong())))
        
        if (currentMatch.matchType == MatchMode.FFA) {
            val totalRounds = (currentMatch as FFAMatch).laps
            activeMatchView?.setRoundsValue(MainApplication.instance.getString(R.string.Round) + " ${synchronisation.laps}/${totalRounds}")
        }

        if (synchronisation.time <= 10000) {
            Log.d("POTATO", "Time less than 10 seconds!")
            activeMatchView?.pulseRemainingTime()
        }
    }

    private fun onMatchPlayersUpdated() {
        activeMatchView?.notifyPlayersChanged()
    }

    private fun onGuessedWordRight(playerGuessedWord: PlayerGuessedWord) {
        activeMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_right)
        activeMatchView?.showConfetti()
    }

    private fun onGuessedWordWrong() {
        SoundManager.playSoundEffect(MainApplication.instance.getContext(), R.raw.sound_effect_word_guessed_wrong)
    }

    private fun onTimesUp() {
        activeMatchView?.hideCanvas()
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
            EventType.TIMES_UP -> onTimesUp()
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