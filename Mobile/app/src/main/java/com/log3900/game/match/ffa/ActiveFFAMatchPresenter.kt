package com.log3900.game.match.ffa

import android.os.Handler
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.match.*
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
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
        activeFFAMatchView.showHintButton(false)
        activeFFAMatchView.setTurnsValue(MainApplication.instance.getString(R.string.turn) + " 1/${FFAMatchManager.getCurrentMatch().laps}")

        matchManager.notifyReadyToPlay()
    }

    override fun onMatchSynchronisation(synchronisation: Synchronisation) {
        super.onMatchSynchronisation(synchronisation)
        val totalRounds = FFAMatchManager.getCurrentMatch().laps
        activeFFAMatchView?.setTurnsValue(MainApplication.instance.getString(R.string.turn) + " ${synchronisation.laps}/${synchronisation.lapTotal}")
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
        activeFFAMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.ffa_guessed_correctly))
        activeFFAMatchView?.showCanvasMessageView(true)
        Handler().postDelayed({
            activeFFAMatchView?.showCanvasMessageView(false)
        }, 2000)
    }

    override fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        super.onPlayerTurnToDraw(playerTurnToDraw)
        activeFFAMatchView?.clearAllPlayerStatusRes()
        activeFFAMatchView?.setPlayerStatus(playerTurnToDraw.userID, R.drawable.ic_edit_black)

        val drawingPlayer = FFAMatchManager.getCurrentMatch().players.find { it.ID == playerTurnToDraw.userID }

        if (drawingPlayer!!.isCPU) {
            activeFFAMatchView?.showHintButton(true)
        } else {
            activeFFAMatchView?.showHintButton(false)
        }

        activeFFAMatchView?.setCanvasMessage(MainApplication.instance.getContext().getString(R.string.ffa_is_drawing_the_next_word, drawingPlayer.username))
        activeFFAMatchView?.showCanvasMessageView(true)

        Handler().postDelayed({
            activeMatchView?.showCanvasMessageView(false)
        }, 2000)
    }

    override fun onRoundEnded(roundEnded: RoundEnded) {
        super.onRoundEnded(roundEnded)

        val scores: ArrayList<Pair<String, Int>> = arrayListOf()
        matchManager.getCurrentMatch().players.forEach { currentMatchPlayer ->
            scores.add(Pair(currentMatchPlayer.username, roundEnded.players.find { currentMatchPlayer.ID == it.userID }!!.newPoints))
        }
        activeFFAMatchView?.showCanvasMessageView(false)
        activeFFAMatchView?.enableDrawFunctions(false, null)
        activeMatchView?.showRoundEndInfoView(roundEnded.word, scores)
        //activeFFAMatchView?.setTimeValue()
        changeRemainingTime(0)

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

        Handler().postDelayed({
            if (!matchEnded) {
                activeFFAMatchView?.hideCanvas()
                activeFFAMatchView?.hideRoundEndInfoView()
                activeFFAMatchView?.showCanvasMessageView(false)
                Handler().postDelayed({
                    activeFFAMatchView?.clearCanvas()
                    activeFFAMatchView?.showCanvas()
                }, 500)
            }
        }, 3000)
    }

    override fun onMatchEnded(matchEnded: MatchEnded) {
        super.onMatchEnded(matchEnded)

        activeFFAMatchView?.hideRoundEndInfoView()

        activeFFAMatchView?.showCanvasMessageView(false)

        val playerScores: ArrayList<Pair<String, Int>> = arrayListOf()
        matchEnded.players.forEach {
            playerScores.add(Pair(it.username, it.points))
        }
        activeMatchView?.showMatchEndInfoView(matchEnded.winnerName, playerScores, AccountRepository.getInstance().getAccount().ID == matchEnded.winner)
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

        val playerPosition = matchManager.getCurrentMatch().players.indexOfFirst { it.ID == playerID }
        activeFFAMatchView?.showPlayerScoredChangedAnimation(formattedScoreChange, variation > 0, playerPosition)
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