package com.log3900.chat

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import com.log3900.chat.Channel.Channel
import com.log3900.chat.Channel.ChannelManager
import com.log3900.chat.Message.MessageManager
import com.log3900.chat.Message.MessageRepository
import com.log3900.chat.Message.ReceivedMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

class ChatManager : Service() {
    private var channelManager: ChannelManager? = null
    private var messageManager: MessageManager? = null
    var ready: Boolean = false
    var subject: PublishSubject<Boolean> = PublishSubject.create()

    private val binder = ChatManagerBinder()

    companion object {
        var instance: ChatManager? = null
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
            setActiveChannel(channelManager?.activeChannel!!)
            setReadyState()
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun setActiveChannel(channel: Channel) {
        println("changed channel")
        channelManager?.activeChannel = channel
        EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_CHANGED, channel))
    }

    fun getActiveChannel(): Channel {
        return channelManager?.activeChannel!!
    }

    fun getCurrentChannelMessages() = Single.create<LinkedList<ReceivedMessage>> {
        it.onSuccess(messageManager?.getMessages(channelManager?.activeChannel!!)!!)
    }

    fun sendMessage(message: String) {
        messageManager?.sendMessage(channelManager?.activeChannel?.ID!!, message)
    }

    private fun setReadyState() {
        ready = true
        subject.onNext(true)
    }


    inner class ChatManagerBinder : Binder() {
        fun getService(): ChatManager = this@ChatManager
    }
}