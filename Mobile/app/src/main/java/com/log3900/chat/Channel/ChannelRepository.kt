package com.log3900.chat.Channel

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.chat.ChatRestService
import com.log3900.chat.Message.MessageRepository
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class ChannelRepository : Service() {
    private val binder = ChannelRepositoryBinder()
    private var socketService: SocketService? = null
    private var subscribers: ConcurrentHashMap<Event, ArrayList<Handler>> = ConcurrentHashMap()
    private lateinit var channelCache: ChannelCache

    companion object {
        var instance: ChannelRepository? = null
    }

    fun getChannels(sessionToken: String): Single<ArrayList<Channel>> {
        return Single.create {
            val call = ChatRestService.service.getChannels(sessionToken, "EN")
            call.enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .add(UUIDAdapter())
                        .build()

                    val adapter: JsonAdapter<List<Channel>> = moshi.adapter(Types.newParameterizedType(List::class.java, Channel::class.java))
                    val res = adapter.fromJson(response.body().toString())
                    it.onSuccess(res as ArrayList<Channel>)
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    println("onFailure")
                }
            })
        }
    }

    fun getJoinedChannels(sessionToken: String): Single<ArrayList<Channel>> {
        return Single.create {
            if (!channelCache.needsReload) {
                it.onSuccess(channelCache.joinedChannels)
            } else {
                getChannels(sessionToken).subscribe(
                    { channels ->
                        channelCache.reloadChannels(channels)
                        it.onSuccess(channelCache.joinedChannels)
                    },
                    { error ->

                    }
                )
            }
        }
    }

    fun getAvailableChannels(sessionToken: String): Single<ArrayList<Channel>> {
        return Single.create {
            if (!channelCache.needsReload) {
                it.onSuccess(channelCache.joinedChannels)
            } else {
                getChannels(sessionToken).subscribe(
                    { channels ->
                        channelCache.reloadChannels(channels)
                        it.onSuccess(channelCache.availableChannels)
                    },
                    { error ->

                    }
                )
            }
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
        channelCache.removeAvailableChannel(channel)
        channelCache.addJoinedChannel(channel)
        socketService?.sendMessage(Event.JOIN_CHANNEL, UUIDUtils.uuidToByteArray(channel.ID))
    }

    fun unsubscribeFromChannel(channel: Channel) {
        channelCache.removeJoinedChannel(channel)
        channelCache.addAvailableChannel(channel)
        socketService?.sendMessage(Event.LEAVE_CHANNEL, UUIDUtils.uuidToByteArray(channel.ID))
    }

    fun createChannel(channelName: String) {
        val dataObject = JsonObject()
        dataObject.addProperty("ChannelName", channelName)
        SocketService.instance?.sendJsonMessage(Event.CREATE_CHANNEL, dataObject.toString())
    }

    private fun onChannelCreated(message: Message) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
        })
        val channelCreated = moshi.unpack<ChannelCreatedMessage>(message.data)
        val channel = Channel(channelCreated.channelID, channelCreated.name, arrayOf())
        channelCache.addAvailableChannel(Channel(channelCreated.channelID, channelCreated.name, arrayOf()))
        EventBus.getDefault().post(MessageEvent(EventType.CHANNEL_CREATED, channel))
    }

    private fun onChannelDeleted(message: Message) {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        socketService = SocketService.instance
        channelCache = ChannelCache()

        Thread(Runnable {
            Looper.prepare()
            socketService?.subscribeToMessage(Event.CHANNEL_CREATED, Handler {
                onChannelCreated(it.obj as Message)
                true
            })
            socketService?.subscribeToMessage(Event.CHANNEL_DELETED, Handler {
                onChannelDeleted(it.obj as Message)
                true
            })
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketService = null
        instance = null
    }


    inner class ChannelRepositoryBinder : Binder() {
        fun getService(): ChannelRepository = this@ChannelRepository
    }
}

class ChannelCreatedMessage(@Json(name = "ChannelName") var name: String, @Json(name = "ChannelID") var channelID: UUID,
                                  @Json(name = "Username") var username: String, @Json(name = "UserID") var userID: UUID,
                                  @Json(name = "Timestamp") var timestamp: Date) {}