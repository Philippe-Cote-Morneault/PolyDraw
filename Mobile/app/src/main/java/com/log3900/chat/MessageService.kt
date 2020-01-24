package com.log3900.chat

import android.os.Handler
import java.util.concurrent.ConcurrentHashMap

enum class MessageEvent {
    MESSAGE_RECEIVED
}

class MessageService {
    private var subscribers: ConcurrentHashMap<MessageEvent, ArrayList<Handler>> = ConcurrentHashMap()
    // TODO: Add SocketService when it is implemented
    //private lateinit var socketService: SocketService

    constructor() {
       initialize()
    }

    fun sendMessage(message: Message) {
        // TODO: Make call to socket service to send message.
    }

    fun subscribe(event: MessageEvent, handler: Handler) {
        if (!subscribers.containsKey(event)) {
            subscribers[event] = ArrayList()
        }

        subscribers[event]?.add(handler)
    }

    fun receiveMessage(message: Message) {

    }

    private fun initialize() {
        // TODO: Make call to socket service to listen to message receiving event and pass receiveMessage function.
    }
}