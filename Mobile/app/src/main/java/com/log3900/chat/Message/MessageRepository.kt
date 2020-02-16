package com.log3900.chat.Message

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonObject
import com.log3900.chat.ChatRestService
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.user.UserRepository
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Completable
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
        MESSAGE_RECEIVED
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

    fun getChannelMessages(channelID: UUID): Single<LinkedList<ReceivedMessage>> {
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

    fun getChannelMessages(channelID: UUID, startIndex: Int, endIndex: Int): Single<LinkedList<ReceivedMessage>> {
        return Single.create {
                val call = ChatRestService.service.getChannelMessages(
                    UserRepository.getUser().sessionToken,
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
                                it.onSuccess(LinkedList(messages))
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
        sessionToken = UserRepository.getUser().sessionToken

        Thread(Runnable {
            Looper.prepare()
            socketService?.subscribeToMessage(com.log3900.socket.Event.MESSAGE_RECEIVED, Handler {
                receiveMessage(it.obj as Message)
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
        val tempMessage = android.os.Message()
        tempMessage.what = Event.MESSAGE_RECEIVED.ordinal
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
        })
        tempMessage.obj = moshi.unpack(message.data) as ReceivedMessage
        addMessageToCache(tempMessage.obj as ReceivedMessage)
        notifySubscribers(Event.MESSAGE_RECEIVED, tempMessage)
    }

    private fun addMessageToCache(message: ReceivedMessage) {
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

    inner class MessageRepositoryBinder : Binder() {
        fun getService(): MessageRepository = this@MessageRepository
    }
}