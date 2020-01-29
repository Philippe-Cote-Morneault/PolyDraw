package com.log3900.chat

import android.os.Handler
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

enum class MessageEvent {
    MESSAGE_RECEIVED
}

class MessageService {
    private var subscribers: ConcurrentHashMap<MessageEvent, ArrayList<Handler>> = ConcurrentHashMap()
    private lateinit var currentChannel : Channel
    // TODO: Add SocketService when it is implemented
    //private lateinit var socketService: SocketService

    constructor() {
       initialize()
    }

    fun sendMessage(message: Message) {
        // TODO: Make call to socket service to send message.
        //socketService.sendMessage(message)
    }

    fun sendMessage(messageText: String) {
        Message(messageText, currentChannel.ID, UUID.randomUUID(), "username", Date())
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

    fun receiveMessage(message: Message) {
        val tempMessage = android.os.Message()
        tempMessage.what = MessageEvent.MESSAGE_RECEIVED.ordinal
        notifySubscribers(MessageEvent.MESSAGE_RECEIVED, android.os.Message())
    }

    private fun initialize() {
        // TODO: Make call to socket service to listen to message receiving event and pass receiveMessage function.
        //socketService.subscribe(SocketEvent.MessageReceived, handler)

        // TODO: Make rest call to get all channels the user can join
        currentChannel = Channel("General", UUID.randomUUID())
    }
}