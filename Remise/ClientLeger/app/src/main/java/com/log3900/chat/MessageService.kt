package com.log3900.chat

import android.os.Handler
import com.daveanthonythomas.moshipack.MoshiPack
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

enum class MessageEvent {
    MESSAGE_RECEIVED
}

class MessageService {
    private var subscribers: ConcurrentHashMap<MessageEvent, ArrayList<Handler>> = ConcurrentHashMap()
    //private lateinit var currentChannel : Channel
    private lateinit var socketService: SocketService

    constructor() {
       initialize()
    }

    fun sendMessage(message: SentMessage) {
        socketService.sendSerializedMessage(Event.MESSAGE_SENT, message)
    }

    fun sendMessage(messageText: String) {
        val message = SentMessage(messageText, UUID.randomUUID())
        sendMessage(message)
    }

    fun subscribe(event: MessageEvent, handler: Handler) {
        if (!subscribers.containsKey(event)) {
            subscribers[event] = ArrayList()
        }

        subscribers[event]?.add(handler)
    }

    fun notifySubscribers(event: MessageEvent, message: android.os.Message) {
        if (subscribers.containsKey(event)) {
            val handlers = subscribers[event]
            for (handler in handlers.orEmpty()) {
                handler.sendMessage(message)
            }
        }
    }

    fun receiveMessage(message: com.log3900.socket.Message) {
        val tempMessage = android.os.Message()
        tempMessage.what = MessageEvent.MESSAGE_RECEIVED.ordinal
        val moshi = MoshiPack({
            add(TimeStampAdapter())
            add(UUIDAdapter())
        })
        tempMessage.obj = moshi.unpack(message.data) as ReceivedMessage
        notifySubscribers(MessageEvent.MESSAGE_RECEIVED, tempMessage)
    }

    private fun initialize() {
        socketService = SocketService.instance!!

        socketService.subscribeToMessage(Event.MESSAGE_RECEIVED, Handler {
            receiveMessage(it.obj as com.log3900.socket.Message)
            true
        })

        // TODO: Make rest call to get all channels the user can join
        //currentChannel = Channel("General", UUID.randomUUID())
    }
}