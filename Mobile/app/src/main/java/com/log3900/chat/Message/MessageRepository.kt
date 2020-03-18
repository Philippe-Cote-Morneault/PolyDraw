package com.log3900.chat.Message

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.navigation.common.R
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonObject
import com.log3900.chat.ChatMessage
import com.log3900.chat.ChatRestService
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.user.AccountRepository
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import retrofit2.Call
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.HashSet

class MessageRepository : Service() {
    enum class Event {
        CHAT_MESSAGE_RECEIVED,
    }

    // Service
    private val binder = MessageRepositoryBinder()
    private var socketService: SocketService? = null
    private var subscribers: ConcurrentHashMap<Event, ArrayList<Handler>> = ConcurrentHashMap()
    private lateinit var sessionToken: String

    // Data
    private val messageCache: MessageCache = MessageCache()
    private val fullyLoadedHistory: HashSet<UUID> = HashSet()

    companion object {
        var instance: MessageRepository? = null
    }

    fun getChannelMessages(channelID: UUID): Single<LinkedList<ChatMessage>> {
        return Single.create {
            if (messageCache.getMessages(channelID).size == 0) {
                getChannelMessages(channelID, 0, 50).subscribe(
                    { messages ->
                        messageCache.prependMessage(channelID, LinkedList(messages))
                        it.onSuccess(messageCache.getMessages(channelID))
                    },
                    {

                    }
                )
            } else {
                it.onSuccess(messageCache.getMessages(channelID))
            }
        }
    }

    fun getChannelMessages(channelID: UUID, startIndex: Int, endIndex: Int): Single<LinkedList<ChatMessage>> {
        return Single.create {
                val call = ChatRestService.service.getChannelMessages(
                    AccountRepository.getAccount().sessionToken,
                    "EN",
                    channelID.toString(),
                    startIndex,
                    endIndex
                )
                call.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        when (response.code()) {
                            200 -> {
                                val moshi = Moshi.Builder()
                                    .add(KotlinJsonAdapterFactory())
                                    .add(UUIDAdapter())
                                    .add(TimeStampAdapter())
                                    .build()
                                val adapter: JsonAdapter<List<ReceivedMessage>> =
                                    moshi.adapter(
                                        Types.newParameterizedType(
                                            List::class.java,
                                            ReceivedMessage::class.java
                                        )
                                    )
                                val messages = adapter.fromJson(response.body()!!.getAsJsonArray("Messages").toString())
                                it.onSuccess(ChatMessage.fromReceivedMessages(messages!!))
                            }
                            else -> {
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        println("ONFAILURE")
                    }
                })
            }
    }

    fun sendMessage(message: SentMessage) {
        socketService?.sendSerializedMessage(com.log3900.socket.Event.MESSAGE_SENT, message)
    }

    fun sendMessage(messageText: String) {
        val message = SentMessage(messageText, UUID.randomUUID())
        sendMessage(message)
    }

    fun loadMoreMessages(count: Int, channelID: UUID): Single<Int> {
        return Single.create {
            if (fullyLoadedHistory.contains(channelID)) {
                it.onSuccess(0)
            } else {
                getChannelMessages(
                    channelID,
                    messageCache.getMessages(channelID).size,
                    messageCache.getMessages(channelID).size + count
                ).subscribe(
                    { messages ->
                        if (messages.size == 0) {
                            fullyLoadedHistory.add(channelID)
                        } else {
                            messageCache.prependMessage(channelID, messages)
                        }

                        it.onSuccess(messages.size)
                    },
                    {

                    }
                )
            }
        }
    }

    fun subscribe(event: Event, handler: Handler) {
        if (!subscribers.containsKey(event)) {
            subscribers[event] = ArrayList()
        }

        subscribers[event]?.add(handler)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        socketService = SocketService.instance
        sessionToken = AccountRepository.getAccount().sessionToken

        Thread(Runnable {
            Looper.prepare()
            socketService?.subscribeToMessage(com.log3900.socket.Event.MESSAGE_RECEIVED, Handler {
                receiveMessage(it.obj as Message)
                true
            })
            socketService?.subscribeToMessage(com.log3900.socket.Event.JOINED_CHANNEL, Handler {
                onUserJoinedChannel(it.obj as Message)
                true
            })
            socketService?.subscribeToMessage(com.log3900.socket.Event.LEFT_CHANNEL, Handler {
                onUserLeftChannel(it.obj as Message)
                true
            })
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketService = null
    }

    private fun receiveMessage(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
            add(KotlinJsonAdapterFactory())
        })
        val chatMessage = ChatMessage.fromReceivedMessage(moshi.unpack(message.data) as ReceivedMessage)
        addMessageToCache(chatMessage)
        val osMessage = android.os.Message()
        osMessage.what = Event.CHAT_MESSAGE_RECEIVED.ordinal
        osMessage.obj = chatMessage
        notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
    }

    private fun addMessageToCache(message: ChatMessage) {
        messageCache.appendMessage(message)
    }

    private fun notifySubscribers(event: Event, message: android.os.Message) {
        if (subscribers.containsKey(event)) {
            val handlers = subscribers[event]
            for (handler in handlers.orEmpty()) {
                val messageCopy = android.os.Message()
                messageCopy.what = message.what
                messageCopy.obj = message.obj
                handler.sendMessage(messageCopy)
            }
        }
    }

    private fun onUserJoinedChannel(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
            add(KotlinJsonAdapterFactory())
        })
        val userJoinedChannelMessage = moshi.unpack(message.data) as UserJoinedChannelMessage
        val messageEvent = EventMessage(String.format(resources.getString(com.log3900.R.string.chat_user_joined_channel_message), userJoinedChannelMessage.username))
        val chatMessage = ChatMessage.fromEventMessage(messageEvent, userJoinedChannelMessage.channelID)
        addMessageToCache(chatMessage)
        val osMessage = android.os.Message()
        osMessage.obj = chatMessage
        notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
    }

    private fun onUserLeftChannel(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
            add(KotlinJsonAdapterFactory())
        })
        val userLeftChannelMessage = moshi.unpack(message.data) as UserLeftChannelMessage
        val messageEvent = EventMessage(String.format(resources.getString(com.log3900.R.string.chat_user_left_channel_message), userLeftChannelMessage.username))
        val chatMessage = ChatMessage.fromEventMessage(messageEvent, userLeftChannelMessage.channelID)
        addMessageToCache(chatMessage)
        val osMessage = android.os.Message()
        osMessage.obj = chatMessage
        notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
    }
    
    inner class MessageRepositoryBinder : Binder() {
        fun getService(): MessageRepository = this@MessageRepository
    }
}

data class UserJoinedChannelMessage(@Json(name = "UserID") var userID: UUID, @Json(name = "Username") var username: String,
                                    @Json(name = "ChannelID") var channelID: UUID, @Json(name = "Timestamp") var timestamp: Date)

data class UserLeftChannelMessage(@Json(name = "UserID") var userID: UUID, @Json(name = "Username") var username: String,
                                    @Json(name = "ChannelID") var channelID: UUID, @Json(name = "Timestamp") var timestamp: Date)