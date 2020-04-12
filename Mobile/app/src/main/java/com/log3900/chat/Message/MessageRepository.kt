package com.log3900.chat.Message

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.log3900.chat.Channel.Channel
import com.log3900.chat.ChatMessage
import com.log3900.chat.ChatRestService
import com.log3900.game.match.HintResponse
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.user.UserRepository
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.MatchAdapter
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.log3900.utils.format.moshi.UserAdapter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class MessageRepository : Service() {
    enum class Event {
        CHAT_MESSAGE_RECEIVED,
    }

    // Service
    private val binder = MessageRepositoryBinder()
    private var socketService: SocketService? = null
    private var subscribers: ConcurrentHashMap<Event, ArrayList<Handler>> = ConcurrentHashMap()
    private var socketMessageHandler: Handler? = null

    // Data
    private val messageCache: MessageCache = MessageCache()
    private val fullyLoadedHistory: HashSet<UUID> = HashSet()
    private var matchChannel: Channel? = null

    companion object {
        var instance: MessageRepository? = null
    }

    fun getChannelMessages(channelID: UUID): Single<LinkedList<ChatMessage>> {
        return Single.create {
            if (!messageCache.isHistoryFetched(channelID)) {
                messageCache.setHistoryFetchedState(channelID, true)
                getChannelMessages(channelID, messageCache.getMessages(channelID).size,  messageCache.getMessages(channelID).size + 50).subscribe(
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
                    AccountRepository.getInstance().getAccount().sessionToken,
                    LanguageManager.getCurrentLanguageCode(),
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

    fun subscribe(event: Event, handler: Handler): Handler {
        if (!subscribers.containsKey(event)) {
            subscribers[event] = ArrayList()
        }

        subscribers[event]?.add(handler)

        return handler
    }

    fun unsubscribe(event: Event, handler: Handler) {
        subscribers[event]?.forEach {
            if (it == handler) {
                subscribers[event]?.remove(it)
            }
        }
    }

    private fun handleSocketMessage(message: android.os.Message) {
        val socketMessage = message.obj as Message

        when (socketMessage.type) {
            com.log3900.socket.Event.MESSAGE_RECEIVED -> receiveMessage(socketMessage)
            com.log3900.socket.Event.JOINED_CHANNEL -> onUserJoinedChannel(socketMessage)
            com.log3900.socket.Event.LEFT_CHANNEL -> onUserLeftChannel(socketMessage)
            com.log3900.socket.Event.HINT_RESPONSE -> onHintResponse(socketMessage)
            com.log3900.socket.Event.USERNAME_CHANGED -> onUsernameChanged(socketMessage)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        socketService = SocketService.instance

        Thread(Runnable {
            Looper.prepare()
            socketMessageHandler = Handler {
                handleSocketMessage(it)
                true
            }
            socketService?.subscribeToMessage(com.log3900.socket.Event.MESSAGE_RECEIVED, socketMessageHandler!!)
            socketService?.subscribeToMessage(com.log3900.socket.Event.JOINED_CHANNEL, socketMessageHandler!!)
            socketService?.subscribeToMessage(com.log3900.socket.Event.LEFT_CHANNEL, socketMessageHandler!!)
            socketService?.subscribeToMessage(com.log3900.socket.Event.HINT_RESPONSE, socketMessageHandler!!)
            socketService?.subscribeToMessage(com.log3900.socket.Event.USERNAME_CHANGED, socketMessageHandler!!)
            EventBus.getDefault().register(this)
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.USERNAME_CHANGED, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.HINT_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.MESSAGE_RECEIVED, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.JOINED_CHANNEL, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.LEFT_CHANNEL, socketMessageHandler!!)
        EventBus.getDefault().unregister(this)
        socketMessageHandler = null
        socketService = null
        instance = null
        super.onDestroy()
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

        if (userJoinedChannelMessage.userID != AccountRepository.getInstance().getAccount().ID) {
            val messageEvent = EventMessage(EventMessage.Type.USER_JOINED_CHANNEL, String.format(resources.getString(com.log3900.R.string.chat_user_joined_channel_message), userJoinedChannelMessage.username))
            val chatMessage = ChatMessage.fromEventMessage(messageEvent, userJoinedChannelMessage.channelID)
            addMessageToCache(chatMessage)
            val osMessage = android.os.Message()
            osMessage.obj = chatMessage
            notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
        }
    }

    private fun onUserLeftChannel(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
            add(KotlinJsonAdapterFactory())
        })
        val userLeftChannelMessage = moshi.unpack(message.data) as UserLeftChannelMessage
        if (userLeftChannelMessage.userID == AccountRepository.getInstance().getAccount().ID) {
            messageCache.removeEntry(userLeftChannelMessage.channelID)
        } else {
            val messageEvent = EventMessage(EventMessage.Type.USER_LEFT_CHANNEL, String.format(resources.getString(com.log3900.R.string.chat_user_left_channel_message), userLeftChannelMessage.username))
            val chatMessage = ChatMessage.fromEventMessage(messageEvent, userLeftChannelMessage.channelID)
            addMessageToCache(chatMessage)
            val osMessage = android.os.Message()
            osMessage.obj = chatMessage
            notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
        }
    }

    private fun onHintResponse(message: Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        Log.d("POTATO", "Hint response = $json")
        val hintResponse = MatchAdapter.jsonToHintResponse(jsonObject)
        var hint = hintResponse.hint

        if (hint.isEmpty()) {
            hint = hintResponse.error
        }

        if (matchChannel != null) {
            UserRepository.getInstance().getUser(hintResponse.botID!!).subscribe(
                {
                    val receivedMessage = ReceivedMessage(hint, matchChannel!!.ID, hintResponse.botID!!, it.username, Date())
                    val chatMessage = ChatMessage(ChatMessage.Type.RECEIVED_MESSAGE, receivedMessage, matchChannel!!.ID)
                    addMessageToCache(chatMessage)
                    val osMessage = android.os.Message()
                    osMessage.obj = chatMessage
                    notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
                },
                {

                }
            )
        }
    }

    private fun onUsernameChanged(message: Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val usernameChanged = UserAdapter.jsonToUsernameChanged(jsonObject)

        if (usernameChanged.userID == AccountRepository.getInstance().getAccount().ID) {
            return
        }

        if (usernameChanged.oldUsername!= "" && usernameChanged.newUsername != usernameChanged.oldUsername) {
            val messageEvent = EventMessage(EventMessage.Type.USERNAME_CHANGED, String.format(resources.getString(com.log3900.R.string.chat_username_changed), usernameChanged.oldUsername, usernameChanged.newUsername))
            val chatMessage = ChatMessage.fromEventMessage(messageEvent, Channel.GENERAL_CHANNEL_ID)
            messageCache.changeEventMessagesForNewUsername(usernameChanged.oldUsername, usernameChanged.newUsername)
            addMessageToCache(chatMessage)
            val osMessage = android.os.Message()
            osMessage.obj = chatMessage
            notifySubscribers(Event.CHAT_MESSAGE_RECEIVED, osMessage)
            EventBus.getDefault().post(MessageEvent(EventType.ALL_MESSAGES_CHANGED, null))
        }
    }

    private fun onChannelCreated(channel: Channel) {
        if (channel.isGame) {
            matchChannel = channel
        }
    }

    private fun onChannelDeleted(channelID: UUID) {
        if (matchChannel != null && matchChannel!!.ID == channelID) {
            messageCache.removeEntry(channelID)
            matchChannel = null
        }
    }

    private fun onLanguageChanged() {
        messageCache.changeEventMessagesToLanguage(LanguageManager.getCurrentLanguageCode())
        EventBus.getDefault().post(MessageEvent(EventType.ALL_MESSAGES_CHANGED, null))
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.CHANNEL_CREATED -> {
                onChannelCreated(event.data as Channel)
            }
            EventType.CHANNEL_DELETED -> {
                onChannelDeleted(event.data as UUID)
            }
            EventType.LANGUAGE_CHANGED -> {
                onLanguageChanged()
            }
        }
    }
    
    inner class MessageRepositoryBinder : Binder() {
        fun getService(): MessageRepository = this@MessageRepository
    }
}

data class UserJoinedChannelMessage(@Json(name = "UserID") var userID: UUID, @Json(name = "Username") var username: String,
                                    @Json(name = "ChannelID") var channelID: UUID, @Json(name = "Timestamp") var timestamp: Date)

data class UserLeftChannelMessage(@Json(name = "UserID") var userID: UUID, @Json(name = "Username") var username: String,
                                    @Json(name = "ChannelID") var channelID: UUID, @Json(name = "Timestamp") var timestamp: Date)