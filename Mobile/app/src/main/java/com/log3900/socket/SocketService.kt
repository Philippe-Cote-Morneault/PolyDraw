package com.log3900.socket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.daveanthonythomas.moshipack.MoshiPack
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class SocketService : Service() {
    private lateinit var socketHandler: SocketHandler
    private var subscribers: ConcurrentHashMap<Event, ArrayList<Handler>> = ConcurrentHashMap()
    private val binder = SocketBinder()

    companion object {
        lateinit var instance: SocketService
    }

    fun sendMessage(event: Event, data: ByteArray) {
        val message = android.os.Message()
        message.what = Request.SEND_MESSAGE.ordinal
        message.obj = Message(event, data)
        socketHandler.sendRequest(message)
    }

    fun sendSerializedMessage(event: Event, data: Any) {
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
        })
        sendMessage(event, moshi.packToByteArray(data))
    }

    fun subscribe(event: Event, handler: Handler) {
        if (!subscribers.containsKey(event)) {
            subscribers[event] = ArrayList()
        }

        subscribers[event]?.add(handler)
    }

    fun startListening() {
        val message = android.os.Message()
        message.what = Request.START_READING.ordinal
        socketHandler.sendRequest(message)
    }

    fun stopListening() {
        val message = android.os.Message()
        message.what = Request.STOP_READING.ordinal
        socketHandler.sendRequest(message)
    }

    private fun notifySubscribers(message: Message){
        val handlerMessage = android.os.Message()
        handlerMessage.what = message.type.eventType.toInt()
        handlerMessage.obj = message

        if (subscribers.containsKey(message.type)) {
            val handlers = subscribers[message.type]
            for (handler in handlers.orEmpty()) {
                handler.sendMessage(handlerMessage)
            }
        }
    }

    private fun onMessageRead(message: android.os.Message) {
        if (message.obj is Message) {
            notifySubscribers(message.obj as Message)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Thread(Runnable {
            Looper.prepare()
            socketHandler = SocketHandler
            socketHandler.createRequestHandler()
            socketHandler.setMessageReadListener(Handler { msg ->
                onMessageRead(msg)
                true
            })
            val req = android.os.Message()
            req.what = Request.START_READING.ordinal
            socketHandler.sendRequest(req)
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        val request = android.os.Message()
        request.what = Request.DISCONNECT.ordinal
        socketHandler.sendRequest(request)
    }

    inner class SocketBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

}