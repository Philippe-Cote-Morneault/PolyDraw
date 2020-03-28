package com.log3900.game.match.ffa

import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.*
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.user.account.AccountRepository
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ActiveFFAMatchPresenter : ActiveMatchPresenter {
    private var FFAMatchManager: FFAMatchManager
    private var activeFFAMatchView: ActiveFFAMatchView? = null

    constructor(activeFFAMatchView: ActiveFFAMatchView) : super(activeFFAMatchView, FFAMatchManager()) {
        FFAMatchManager = matchManager as FFAMatchManager
        this.activeFFAMatchView = activeMatchView as ActiveFFAMatchView

        activeFFAMatchView.setPlayerScores(matchManager.getPlayerScores())
        activeFFAMatchView.enableHintButton(false)

        matchManager.notifyReadyToPlay()
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        val totalRounds = FFAMatchManager.getCurrentMatch().laps
        activeFFAMatchView?.setTurnsValue(MainApplication.instance.getString(R.string.turn) + " ${synchronisation.laps}/${totalRounds}")

    }

    override fun onGuessedWordRight(playerGuessedWord: PlayerGuessedWord) {
        super.onGuessedWordRight(playerGuessedWord)
        activeFFAMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)

        updatePlayerScore(playerGuessedWord.userID, playerGuessedWord.pointsTotal, playerGuessedWord.points)
    }

    override fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        super.onPlayerTurnToDraw(playerTurnToDraw)
        activeFFAMatchView?.clearAllPlayerStatusRes()
        activeFFAMatchView?.setPlayerStatus(playerTurnToDraw.userID, R.drawable.ic_edit_black)
    }

    private fun onTurnToDraw(turnToDraw: TurnToDraw) {
        activeFFAMatchView?.clearCanvas()
        activeFFAMatchView?.clearAllPlayerStatusRes()
        activeFFAMatchView?.setPlayerStatus(AccountRepository.getInstance().getAccount().ID, R.drawable.ic_edit_black)
        activeFFAMatchView?.showWordToDrawView()
        activeFFAMatchView?.setWordToDraw(turnToDraw.word)
        activeFFAMatchView?.enableDrawFunctions(true, turnToDraw.drawingID)
    }

    private fun onPlayerGuessedWord(playerGuessedWord: PlayerGuessedWord) {
        activeFFAMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)

        updatePlayerScore(playerGuessedWord.userID, playerGuessedWord.pointsTotal, playerGuessedWord.points)
    }

    private fun updatePlayerScore(playerID: UUID, newScore: Int, variation: Int) {
        var formattedScoreChange = "+"
        if (variation < 0) {
            formattedScoreChange = "-"
        }

        formattedScoreChange += variation

        val playerPosition = matchManager.getCurrentMatch().players.indexOfFirst { it.ID == playerID }
        activeFFAMatchView?.showPlayerScoredChangedAnimation(formattedScoreChange, variation > 0, playerPosition)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.type) {
            EventType.TURN_TO_DRAW -> onTurnToDraw(event.data as TurnToDraw)
            EventType.PLAYER_GUESSED_WORD -> onPlayerGuessedWord(event.data as PlayerGuessedWord)
        }
    }

    override fun destroy() {
        super.destroy()
        activeFFAMatchView = null
    }
}