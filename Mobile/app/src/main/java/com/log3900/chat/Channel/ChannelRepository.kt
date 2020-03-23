package com.log3900.chat.Channel

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.chat.ChatRestService
import com.log3900.chat.Message.UserJoinedChannelMessage
import com.log3900.chat.Message.UserLeftChannelMessage
import com.log3900.shared.architecture.DialogEventMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.exceptions.BadNetworkResponseException
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.UUIDUtils
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
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

class ChannelRepository : Service() {
    private val binder = ChannelRepositoryBinder()
    private var socketService: SocketService? = null
    private var socketMessageHandler: Handler? = null
    private lateinit var channelCache: ChannelCache

    var isReady = false

    companion object {
        var instance: ChannelRepository? = null
    }

    fun initializeRepository() {
        instance = this
        socketService = SocketService.instance
        channelCache = ChannelCache()
        getChannels(AccountRepository.getInstance().getAccount().sessionToken).subscribe(
            {
                channelCache.reloadChannels(it)
                isReady = true
            },
            {

            }
        )
    }

    fun getChannels(sessionToken: String): Single<ArrayList<Channel>> {
        return Single.create {
            val call = ChatRestService.service.getChannels(sessionToken, "EN")
            call.enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    when (response.code()) {
                        200 -> {
                            val channels = com.log3900.chat.Channel.JsonAdapter.jsonToChannels(response.body()!!)
                            it.onSuccess(channels)
                        }
                        else -> {
                            it.onError(BadNetworkResponseException(response.errorBody()?.string()?: "getChannels failure"))
                        }
                    }
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    it.onError(BadNetworkResponseException(t.message?: "getChannels failure"))
                }
            })
        }
    }

    fun getJoinedChannels(sessionToken: String): Single<ArrayList<Channel>> {
        return Single.create {
            getChannels(sessionToken).subscribe(
                { channels ->
                    channelCache.reloadChannels(channels)
                    it.onSuccess(channelCache.joinedChannels)
                },
                { throwable ->
                    it.onError(throwable)
                }
            )
        }
    }

    fun getAvailableChannels(sessionToken: String): Single<ArrayList<Channel>> {
        return Single.create {
            getChannels(sessionToken).subscribe(
                { channels ->
                    channelCache.reloadChannels(channels)
                    it.onSuccess(channelCache.availableChannels)
                },
                { throwable ->
                    it.onError(throwable)
                }
            )
        }
    }

    fun getChannel(sessionToken: String, channelID: String) {
        var channel: Channel? = null
        val call = ChatRestService.service.getChannel(sessionToken, "EN", channelID)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .add(UUIDAdapter())
                    .build()

                val adapter: JsonAdapter<Channel> = moshi.adapter(Channel::class.java)
                channel = adapter.fromJson(response.body().toString())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })
    }

    fun subscribeToChannel(channel: Channel) {
        socketService?.sendMessage(Event.JOIN_CHANNEL, UUIDUtils.uuidToByteArray(channel.ID))
    }

    fun unsubscribeFromChannel(channel: Channel) {
        socketService?.sendMessage(Event.LEAVE_CHANNEL, UUIDUtils.uuidToByteArray(channel.ID))
    }

    fun createChannel(channelName: String) {
        val dataObject = JsonObject()
        dataObject.addProperty("ChannelName", channelName)
        SocketService.instance?.sendJsonMessage(Event.CREATE_CHANNEL, dataObject.toString())
    }

    fun deleteChannel(channel: Channel) {
        socketService?.sendMessage(Event.DELETE_CHANNEL, UUIDUtils.uuidToByteArray(channel.ID))
    }

    private fun onChannelCreated(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
        })
        val channelCreated = moshi.unpack<ChannelCreatedMessage>(message.data)
        val channel = Channel(channelCreated.channelID, channelCreated.name, channelCreated.isGame, arrayListOf(ChannelUser(channelCreated.username, channelCreated.userID)))

        if (channel.isGame) {
            channel.name = MainApplication.instance.resources.getString(R.string.game_channel_name)
        }

        channelCache.addAvailableChannel(channel)
        EventBus.getDefault().post(MessageEvent(EventType.CHANNEL_CREATED, channel))
    }

    private fun onChannelDeleted(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
        })
        val channelCreated = moshi.unpack<ChannelDeletedMessage>(message.data)
        channelCache.removeChannel(channelCreated.channelID)
        EventBus.getDefault().post(MessageEvent(EventType.CHANNEL_DELETED, channelCreated.channelID))
    }

    private fun onChannelJoined(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
            add(KotlinJsonAdapterFactory())
        })
        val userJoinedChannelMessage = moshi.unpack(message.data) as UserJoinedChannelMessage

        if (userJoinedChannelMessage.userID == AccountRepository.getInstance().getAccount().ID) {
            val channel = channelCache.getChannel(userJoinedChannelMessage.channelID)!!
            channelCache.removeAvailableChannel(channel)
            channelCache.addJoinedChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.SUBSCRIBED_TO_CHANNEL, channel))
        }
    }

    private fun onChannelLeft(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
            add(KotlinJsonAdapterFactory())
        })
        val userLeftChannelMessage = moshi.unpack(message.data) as UserLeftChannelMessage

        if (userLeftChannelMessage.userID == AccountRepository.getInstance().getAccount().ID) {
            val channel = channelCache.getChannel(userLeftChannelMessage.channelID)!!
            channelCache.removeJoinedChannel(channel)
            if (!channel.isGame) {
                channelCache.addAvailableChannel(channel)
            }
            EventBus.getDefault().post(MessageEvent(EventType.UNSUBSCRIBED_FROM_CHANNEL, channel))
        }
    }

    private fun onLanguageChanged() {
        val generalChannel = channelCache.getChannel(Channel.GENERAL_CHANNEL_ID)
        if (generalChannel != null) {
            generalChannel.name = MainApplication.instance.resources.getString(R.string.general_channel_name)
        }
    }

    private fun handleSocketMessage(message: android.os.Message) {
        val socketMessage = message.obj as Message

        when (socketMessage.type) {
            Event.CHANNEL_CREATED -> onChannelCreated(socketMessage)
            Event.CHANNEL_DELETED -> onChannelDeleted(socketMessage)
            Event.JOINED_CHANNEL -> onChannelJoined(socketMessage)
            Event.LEFT_CHANNEL -> onChannelLeft(socketMessage)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        initializeRepository()

        Thread(Runnable {
            Looper.prepare()
            socketMessageHandler = Handler {
                handleSocketMessage(it)
                true
            }
            socketService?.subscribeToMessage(Event.CHANNEL_CREATED, socketMessageHandler!!)
            socketService?.subscribeToMessage(Event.CHANNEL_DELETED, socketMessageHandler!!)
            socketService?.subscribeToMessage(Event.JOINED_CHANNEL, socketMessageHandler!!)
            socketService?.subscribeToMessage(Event.LEFT_CHANNEL, socketMessageHandler!!)
            EventBus.getDefault().register(this)
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        socketService?.unsubscribeFromMessage(Event.LEFT_CHANNEL, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.JOINED_CHANNEL, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.CHANNEL_CREATED, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.CHANNEL_DELETED, socketMessageHandler!!)
        EventBus.getDefault().unregister(this)
        socketService = null
        instance = null
        super.onDestroy()
    }

    @Subscribe
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.LANGUAGE_CHANGED -> {
                onLanguageChanged()
            }
        }
    }


    inner class ChannelRepositoryBinder : Binder() {
        fun getService(): ChannelRepository = this@ChannelRepository
    }
}

class ChannelCreatedMessage(@Json(name = "ChannelName") var name: String, @Json(name = "ChannelID") var channelID: UUID,
                            @Json(name = "Username") var username: String, @Json(name = "UserID") var userID: UUID,
                            @Json(name = "IsGame") var isGame: Boolean, @Json(name = "Timestamp") var timestamp: Date) {}

class ChannelDeletedMessage(@Json(name = "ChannelID") var channelID: UUID,
                            @Json(name = "Username") var username: String, @Json(name = "UserID") var userID: UUID,
                            @Json(name = "Timestamp") var timestamp: Date) {}