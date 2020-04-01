package com.log3900.game.match.ffa

import android.os.Handler
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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ActiveFFAMatchPresenter : ActiveMatchPresenter {
    private var FFAMatchManager: FFAMatchManager
    private var activeFFAMatchView: ActiveFFAMatchView? = null
    private var playerScores: HashMap<UUID, Int> = hashMapOf()

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
        matchManager.getPlayerScores().forEach { t, u ->
            if (!playerScores.containsKey(t)) {
                playerScores[t] = u
            }

            if (playerScores[t] != u) {
                val variation = u - playerScores[t]!!
                playerScores[t] = u
                updatePlayerScore(t, u, variation)
            }
        }

    }

    override fun onGuessedWordRight(playerGuessedWord: PlayerGuessedWord) {
        super.onGuessedWordRight(playerGuessedWord)
        activeFFAMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)
    }

    override fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        super.onPlayerTurnToDraw(playerTurnToDraw)
        activeFFAMatchView?.clearAllPlayerStatusRes()
        activeFFAMatchView?.setPlayerStatus(playerTurnToDraw.userID, R.drawable.ic_edit_black)

        val drawingPlayer = FFAMatchManager.getCurrentMatch().players.find { it.ID == playerTurnToDraw.userID }

        if (drawingPlayer!!.isCPU) {
            activeFFAMatchView?.enableHintButton(true)
        } else {
            activeFFAMatchView?.enableHintButton(false)
        }

        activeFFAMatchView?.setCanvasMessage(drawingPlayer.username + " is drawing the next word!")
        activeFFAMatchView?.showCanvasMessageView(true)

        Handler().postDelayed({
            activeMatchView?.showCanvasMessageView(false)
        }, 2000)
    }

    override fun onRoundEnded(roundEnded: RoundEnded) {
        super.onRoundEnded(roundEnded)

        roundEnded.players.forEach {
            if (playerScores[it.userID] != it.points) {
                var variation = it.newPoints
                if (playerScores[it.userID] != null) {
                    variation = it.points - playerScores[it.userID]!!
                }
                updatePlayerScore(it.userID, it.points, variation)
                playerScores[it.userID] = it.points
            }
        }
    }

    override fun onMatchEnded(matchEnded: MatchEnded) {
        super.onMatchEnded(matchEnded)

        val playerScores: ArrayList<Pair<String, Int>> = arrayListOf()
        matchEnded.players.forEach {
            playerScores.add(Pair(it.username, it.points))
        }
        activeMatchView?.showMatchEndInfoView(matchEnded.winnerName, playerScores)
    }

    private fun onTurnToDraw(turnToDraw: TurnToDraw) {
        activeFFAMatchView?.clearCanvas()
        activeFFAMatchView?.clearAllPlayerStatusRes()
        activeFFAMatchView?.setPlayerStatus(AccountRepository.getInstance().getAccount().ID, R.drawable.ic_edit_black)
        activeFFAMatchView?.showWordToDrawView()
        activeFFAMatchView?.setWordToDraw(turnToDraw.word)
        activeFFAMatchView?.enableDrawFunctions(true, turnToDraw.drawingID)

        activeFFAMatchView?.setCanvasMessage("You are drawing the word ${turnToDraw.word}!")
        activeFFAMatchView?.showCanvasMessageView(true)

        Handler().postDelayed({
            activeMatchView?.showCanvasMessageView(false)
        }, 2000)
    }

    private fun onPlayerGuessedWord(playerGuessedWord: PlayerGuessedWord) {
        activeFFAMatchView?.setPlayerStatus(playerGuessedWord.userID, R.drawable.ic_green_check)
    }

    private fun onMatchStarting() {
        matchManager.getPlayerScores().forEach { t, u ->
            playerScores[t] = u
        }
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

        Handler().postDelayed({
            val playerPosition = matchManager.getCurrentMatch().players.indexOfFirst { it.ID == playerID }
            activeFFAMatchView?.showPlayerScoredChangedAnimation(formattedScoreChange, variation > 0, playerPosition)
        }, 1000)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.type) {
            EventType.TURN_TO_DRAW -> onTurnToDraw(event.data as TurnToDraw)
            EventType.PLAYER_GUESSED_WORD -> onPlayerGuessedWord(event.data as PlayerGuessedWord)
            EventType.MATCH_STARTING -> onMatchStarting()
        }
    }

    override fun destroy() {
        super.destroy()
        activeFFAMatchView = null
    }
}