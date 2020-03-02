package com.log3900.chat

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.log3900.chat.Channel.Channel
import com.log3900.chat.Channel.ChannelManager
import com.log3900.chat.Message.MessageManager
import com.log3900.chat.Message.MessageRepository
import com.log3900.chat.Message.ReceivedMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatManager : Service() {
    private var channelManager: ChannelManager? = null
    private var messageManager: MessageManager? = null

    private val binder = ChatManagerBinder()

    companion object {
        private var isReady = false
        private var isReadySignal:PublishSubject<Boolean> = PublishSubject.create()
        private var instance: ChatManager? = null

        fun getInstance(): Single<ChatManager> {
            return Single.create {
                val readySignal = isReadySignal.subscribe(
                    { _ ->
                        it.onSuccess(instance!!)
                    },
                    {

                    }
                )
                if (isReady) {
                    readySignal.dispose()
                    it.onSuccess(instance!!)
                }
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        channelManager = ChannelManager()
        messageManager = MessageManager()


        Thread(Runnable {
            Looper.prepare()
            channelManager?.init()
            messageManager?.init()
            setActiveChannel(channelManager?.activeChannel!!)
            setReadyState()
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isReady = false
    }

    fun openChat() {
        if (channelManager?.previousChannel != null) {
            setActiveChannel(channelManager?.previousChannel)
        } else {
            setActiveChannel(channelManager?.getDefaultChannel())
        }
    }

    fun closeChat() {
        channelManager?.previousChannel = channelManager?.activeChannel
        setActiveChannel(null)
    }

    fun setActiveChannel(channel: Channel?) {
        if (channel != null) {
            channelManager?.previousChannel = channel
        }
        channelManager?.changeActiveChannel(channel)
    }

    fun getActiveChannel(): Channel? {
        return channelManager?.activeChannel
    }

    fun getCurrentChannelMessages(): Single<LinkedList<ChatMessage>>{
        if (channelManager?.activeChannel != null) {
            return messageManager?.getMessages(channelManager?.activeChannel!!)!!
        } else {
            return Single.create {
                it.onError(NoSuchElementException())
            }
        }
    }

    fun sendMessage(message: String) {
        messageManager?.sendMessage(channelManager?.activeChannel?.ID!!, message)
    }

    fun getJoinedChannels() = Single.create<ArrayList<Channel>> {
        it.onSuccess(channelManager?.joinedChannels!!)
    }

    fun getAvailableChannels() = Single.create<ArrayList<Channel>> {
        it.onSuccess(channelManager?.availableChannels!!)
    }

    fun changeSubscriptionStatus(channel: Channel) {
        channelManager?.changeSubscriptionStatus(channel)
    }

    fun loadMoreMessages(): Single<Int> {
        return messageManager?.loadMoreMessages(channelManager?.activeChannel?.ID!!)!!
    }

    fun createChannel(channelName: String) = Completable.create {
        val res = channelManager?.createChannel(channelName)!!
        if (res) {
            it.onComplete()
        } else {
            it.onError(Exception("Channel already exists"))
        }
    }

    fun deleteChannel(channel: Channel) {
        channelManager?.deleteChannel(channel)
    }

    fun getUnreadMessages(): HashMap<UUID, Int> {
        return channelManager?.getUnreadMessage()!!
    }

    private fun setReadyState() {
        isReady = true
        isReadySignal.onNext(true)
    }

    inner class ChatManagerBinder : Binder() {
        fun getService(): ChatManager = this@ChatManager
    }
}