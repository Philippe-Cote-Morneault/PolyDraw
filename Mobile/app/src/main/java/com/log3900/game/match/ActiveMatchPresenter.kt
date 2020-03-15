package com.log3900.game.match

import android.util.Log
import com.log3900.R
import com.log3900.game.group.Group
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

    private fun onMatchSynchronisation(synchronisation: Synchronisation) {
        activeMatchView?.setTimeValue(DateFormatter.formatDateToTime(Date(synchronisation.time.toLong())))
    }

    private fun onMatchPlayersUpdated() {
        activeMatchView?.notifyPlayersChanged()
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