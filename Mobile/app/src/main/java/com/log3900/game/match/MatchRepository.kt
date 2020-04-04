package com.log3900.game.match

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonParser
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.MatchAdapter
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.HashMap

class MatchRepository : Service() {
    private val binder = MatchRepositoryBinder()
    private var socketService: SocketService? = null
    private var socketMessageHandler: Handler? = null
    private var currentMatch: Match? = null
    private var playerScores: HashMap<UUID, Int> = HashMap()

    var isReady = false

    companion object {
        var instance: MatchRepository? = null
    }

    private fun initializeRepository() {
        instance = this
        socketService = SocketService.instance

        socketService?.subscribeToMessage(Event.PLAYER_GUESSED_WORD, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.GUESS_WORD_RESPONSE, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.TURN_TO_DRAW, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.PLAYER_TURN_TO_DRAW, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.USER_JOINED_GROUP, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.MATCH_ABOUT_TO_START, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.PLAYER_SYNC, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.MATCH_ENDED, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.PLAYER_LEFT_MATCH, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.TIMES_UP, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.CHECKPOINT, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.ROUND_ENDED, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.HINT_RESPONSE, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.TEAMATE_GUESSED_WORD_PROPERLY, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.TEAMATE_GUESSED_WORD_INCORRECTLY, socketMessageHandler!!)
        socketService?.subscribeToMessage(Event.MATCH_CANCELLED, socketMessageHandler!!)
    }

    fun getCurrentMatch(): Match? {
        return currentMatch
    }

    fun getPlayerScores(): HashMap<UUID, Int> {
        return playerScores
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        socketMessageHandler = Handler {
            handleSocketMessage(it)
            true
        }
        initializeRepository()
    }

    private fun handleSocketMessage(message: Message) {
        val socketMessage = message.obj as com.log3900.socket.Message

        when (socketMessage.type) {
            Event.MATCH_ABOUT_TO_START -> onMatchAboutToStart(socketMessage)
            Event.MATCH_STARTING -> onMatchStarting(socketMessage)
            Event.PLAYER_TURN_TO_DRAW -> onPlayerTurnToDraw(socketMessage)
            Event.TURN_TO_DRAW -> onTurnToDraw(socketMessage)
            Event.GUESS_WORD_RESPONSE -> onGuessWordResponse(socketMessage)
            Event.PLAYER_GUESSED_WORD -> onPlayerGuessedWord(socketMessage)
            Event.PLAYER_SYNC -> onPlayerSync(socketMessage)
            Event.MATCH_ENDED -> onMatchEnded(socketMessage)
            Event.PLAYER_LEFT_MATCH -> onPlayerLeftMatch(socketMessage)
            Event.TIMES_UP -> onTimesUp(socketMessage)
            Event.CHECKPOINT -> onCheckpoint(socketMessage)
            Event.ROUND_ENDED -> onRoundEnded(socketMessage)
            Event.HINT_RESPONSE -> onHintResponse(socketMessage)
            Event.TEAMATE_GUESSED_WORD_PROPERLY -> onTeamateGuessedWordProperly(socketMessage)
            Event.TEAMATE_GUESSED_WORD_INCORRECTLY -> onTeamateGuessedWordInproperly(socketMessage)
            Event.MATCH_CANCELLED -> onMatchCancelled(socketMessage)
        }
    }

    fun notifyReadyToPlay() {
        socketService?.sendMessage(Event.READY_TO_PLAY_MATCH, byteArrayOf())
    }

    fun makeGuess(text: String) {
        socketService?.sendMessage(Event.GUESS_WORD, text.toByteArray())
    }

    fun leaveMatch() {
        socketService?.sendMessage(Event.LEAVE_MATCH, byteArrayOf())
    }

    fun requestHint() {
        socketService?.sendMessage(Event.HINT_REQUEST, byteArrayOf())
    }

    private fun onMatchAboutToStart(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val match = MatchAdapter.fromJson(jsonObject)
        currentMatch = match
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_ABOUT_TO_START, match))
    }

    private fun onMatchStarting(message: com.log3900.socket.Message) {
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_STARTING, null))
    }

    private fun onGuessWordResponse(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val validGuess = jsonObject.get("Valid").asBoolean
        if (!validGuess) {
            EventBus.getDefault().post(MessageEvent(EventType.GUESSED_WORD_WRONG, null))
        }
    }

    private fun onPlayerTurnToDraw(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val playerTurnToDraw = MatchAdapter.jsonToPlayerTurnToDraw(jsonObject)
        if (playerTurnToDraw.userID != AccountRepository.getInstance().getAccount().ID) {
            EventBus.getDefault().post(MessageEvent(EventType.PLAYER_TURN_TO_DRAW, playerTurnToDraw))
        }
    }

    private fun onTurnToDraw(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val turnToDraw = MatchAdapter.jsonToTurnToDraw(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.TURN_TO_DRAW, turnToDraw))
    }

    private fun onPlayerGuessedWord(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val playerGuessedWord = MatchAdapter.jsonToPlayerGuessedWord(jsonObject)
        updatePlayerScore(playerGuessedWord.userID, playerGuessedWord.pointsTotal)
        if (playerGuessedWord.userID == AccountRepository.getInstance().getAccount().ID) {
            EventBus.getDefault().post(MessageEvent(EventType.GUESSED_WORD_RIGHT, playerGuessedWord))
        } else {
            EventBus.getDefault().post(MessageEvent(EventType.PLAYER_GUESSED_WORD, playerGuessedWord))
        }
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_PLAYERS_UPDATED, null))
    }

    private fun onPlayerSync(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val synchronisation = MatchAdapter.jsonToSynchronisation(jsonObject)
        Log.d("POTATO", "Sync = $json")

        var playerScoresChanged = false
        synchronisation.players.forEach {
            if (playerScores[it.first] != it.second) {
                updatePlayerScore(it.first, it.second)
                playerScoresChanged = true
            }
        }

        EventBus.getDefault().post(MessageEvent(EventType.MATCH_SYNCHRONISATION, synchronisation))

        if (playerScoresChanged) {
            EventBus.getDefault().post(MessageEvent(EventType.MATCH_PLAYERS_UPDATED, null))
        }
    }

    private fun onMatchEnded(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val matchEnded = MatchAdapter.jsonToMatchEnded(jsonObject)
        Log.d("POTATO", "Match ended = $json")

        matchEnded.players.forEach {
            updatePlayerScore(it.userID, it.points)
        }

        EventBus.getDefault().post(MessageEvent(EventType.MATCH_ENDED, matchEnded))

        EventBus.getDefault().post(MessageEvent(EventType.MATCH_PLAYERS_UPDATED, null))
    }

    private fun onPlayerLeftMatch(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        Log.d("POTATO", "onPlayerLeftMatch json = $json")
        val userID = UUID.fromString(jsonObject.get("UserID").asString)
        if (userID != AccountRepository.getInstance().getAccount().ID) {
            currentMatch?.players?.removeIf {
                it.ID == userID
            }
            playerScores.remove(userID)
            EventBus.getDefault().post(MessageEvent(EventType.MATCH_PLAYERS_UPDATED, null))
        }
    }

    private fun onTimesUp(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val timesUp = MatchAdapter.jsonToTimesUp(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.TIMES_UP, timesUp))
    }

    private fun onCheckpoint(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val checkPoint = MatchAdapter.jsonToCheckpoint(jsonObject)
        Log.d("POTATO", "onCheckpoint json = $json")
        EventBus.getDefault().post(MessageEvent(EventType.CHECKPOINT, checkPoint))
    }

    private fun onRoundEnded(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        Log.d("POTATO", "RoundEnded = $json")
        val roundEnded = MatchAdapter.jsonToRoundEnded(jsonObject)
        roundEnded.players.forEach {
            updatePlayerScore(it.userID, it.points)
        }

        EventBus.getDefault().post(MessageEvent(EventType.ROUND_ENDED, roundEnded))
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_PLAYERS_UPDATED, null))
    }

    private fun onHintResponse(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        Log.d("POTATO", "Hint response = $json")
        val hintResponse = MatchAdapter.jsonToHintResponse(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.HINT_RESPONSE, hintResponse))
    }

    private fun onTeamateGuessedWordProperly(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val teamateGuessedWordProperly = MatchAdapter.jsonToTeamateGuessedProperly(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.TEAMATE_GUESSED_WORD_PROPERLY, teamateGuessedWordProperly))
    }

    private fun onTeamateGuessedWordInproperly(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val teamateGuessedWordInproperly = MatchAdapter.jsonToTeamateGuessedInproperly(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.TEAMATE_GUESSED_WORD_INCORRECTLY, teamateGuessedWordInproperly))
    }

    private fun onMatchCancelled(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val matchCancelled = MatchAdapter.jsonToMatchCancelled(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_CANCELLED, matchCancelled))
    }

    private fun updatePlayerScore(playerID: UUID, newScore: Int) {
        playerScores[playerID] = newScore
        reorderPlayers()
    }

    private fun reorderPlayers() {
        currentMatch?.players?.sortByDescending {
            playerScores[it.ID]
        }
    }

    override fun onDestroy() {
        socketService?.unsubscribeFromMessage(Event.MATCH_CANCELLED, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.TEAMATE_GUESSED_WORD_INCORRECTLY, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.TEAMATE_GUESSED_WORD_PROPERLY, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.HINT_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.ROUND_ENDED, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.CHECKPOINT, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.TIMES_UP, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.PLAYER_LEFT_MATCH, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.PLAYER_SYNC, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.PLAYER_GUESSED_WORD, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.GUESS_WORD_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.TURN_TO_DRAW, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.PLAYER_TURN_TO_DRAW, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.MATCH_ABOUT_TO_START, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.START_MATCH_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.MATCH_ENDED, socketMessageHandler!!)
        socketMessageHandler = null
        socketService = null
        instance = null
        super.onDestroy()
    }

    inner class MatchRepositoryBinder : Binder() {
        fun getService(): MatchRepository = this@MatchRepository
    }
}