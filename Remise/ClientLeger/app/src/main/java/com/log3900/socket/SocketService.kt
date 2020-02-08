package com.log3900.socket

import android.app.Service
import android.content.Intent
import android.os.*
import com.daveanthonythomas.moshipack.MoshiPack
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

enum class SocketEvent {
    CONNECTION_ERROR,
    CONNECTED,
    DISCONNECTED
}

class SocketService : Service() {
    private lateinit var socketHandler: SocketHandler
    private var messageSubscribers: ConcurrentHashMap<Event, ArrayList<Handler>> = ConcurrentHashMap()
    private var eventSubscribers: ConcurrentHashMap<SocketEvent, ArrayList<Handler>> = ConcurrentHashMap()
    private val binder = SocketBinder()

    companion object {
        var instance: SocketService? = null
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

    fun subscribeToMessage(event: Event, handler: Handler) {
        if (!messageSubscribers.containsKey(event)) {
            messageSubscribers[event] = ArrayList()
        }

        messageSubscribers[event]?.add(handler)
    }

    fun subscribeToEvent(event: SocketEvent, handler: Handler) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers[event] = ArrayList()
        }

        eventSubscribers[event]?.add(handler)
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

    private fun notifyMessageSubscribers(message: Message){
        if (messageSubscribers.containsKey(message.type)) {
            val handlers = messageSubscribers[message.type]
            for (handler in handlers.orEmpty()) {
                val handlerMessage = android.os.Message()
                handlerMessage.what = message.type.eventType.toInt()
                handlerMessage.obj = message
                handler.sendMessage(handlerMessage)
            }
        }
    }

    private fun notifyEventSubscribers(event: SocketEvent, message: android.os.Message){
        if (eventSubscribers.containsKey(event)) {
            val handlers = eventSubscribers[event]
            for (handler in handlers.orEmpty()) {
                val messageCopy = android.os.Message()
                messageCopy.what = message.what
                messageCopy.obj = message.obj
                handler.sendMessage(messageCopy)
            }
        }
    }

    fun unsubscribeFromEvent(event: SocketEvent, handler: Handler) {

    }

    private fun onMessageRead(message: android.os.Message) {
        if (message.obj is Message) {
            notifyMessageSubscribers(message.obj as Message)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun connectToSocket() {
        Thread(Runnable {
            Looper.prepare()
            try {
                socketHandler.connect()
                socketHandler.setMessageReadListener(Handler { msg ->
                    onMessageRead(msg)
                    true
                })
                val req = android.os.Message()
                req.what = Request.START_READING.ordinal
                socketHandler.sendRequest(req)
                val message = android.os.Message()
                message.what = SocketEvent.CONNECTED.ordinal
                notifyEventSubscribers(SocketEvent.CONNECTED, message)
                socketHandler.setConnectionErrorListener(Handler { msg ->
                    notifyEventSubscribers(SocketEvent.values()[msg.what], msg)
                    true
                })

                subscribeToMessage(Event.HEALTH_CHECK_SERVER, Handler {msg ->
                    true
                })
            } catch (e: Exception) {
                println("could not connect")
            }
            Looper.loop()
        }).start()
    }

    fun disconnectSocket(handler: Handler?) {
        //val req = android.os.Message()
        //req.what = Request.DISCONNECT.ordinal
        //socketHandler.sendRequest(req)
        if (SocketHandler.state.get() == State.CONNECTED) {
            socketHandler.setDisconnectionListener(Handler {
                handler?.sendEmptyMessage(SocketEvent.DISCONNECTED.ordinal)
                notifyEventSubscribers(SocketEvent.DISCONNECTED, it)
                socketHandler.setDisconnectionListener(null)
                true
            })
            socketHandler.setConnectionErrorListener(null)
            socketHandler.onDisconnect()
        }
    }

    fun getSocketState(): State {
        return socketHandler.state.get()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        socketHandler = SocketHandler
        socketHandler.createRequestHandler()
        connectToSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        val request = android.os.Message()
        request.what = Request.DISCONNECT.ordinal
        socketHandler.sendRequest(request)
        instance = null
    }

    inner class SocketBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

}