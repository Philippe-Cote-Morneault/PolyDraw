package com.log3900.game.match

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.log3900.game.group.*
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.UUIDUtils
import com.log3900.utils.format.moshi.GroupAdapter
import com.log3900.utils.format.moshi.MatchAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    }

    fun getCurrentMatch(): Match? {
        return currentMatch
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
        Log.d("POTATO", json)
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
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_PLAYERS_UPDATED, null))
        EventBus.getDefault().post(MessageEvent(EventType.PLAYER_GUESSED_WORD, playerGuessedWord))
    }

    private fun onPlayerSync(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val synchronisation = MatchAdapter.jsonToSynchronisation(jsonObject)
        EventBus.getDefault().post(MessageEvent(EventType.MATCH_SYNCHRONISATION, synchronisation))
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
        socketService?.unsubscribeFromMessage(Event.PLAYER_SYNC, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.PLAYER_GUESSED_WORD, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.GUESS_WORD_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.TURN_TO_DRAW, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.PLAYER_TURN_TO_DRAW, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.MATCH_ABOUT_TO_START, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.START_MATCH_RESPONSE, socketMessageHandler!!)
        socketMessageHandler = null
        socketService = null
        instance = null
        super.onDestroy()
    }

    inner class MatchRepositoryBinder : Binder() {
        fun getService(): MatchRepository = this@MatchRepository
    }
}