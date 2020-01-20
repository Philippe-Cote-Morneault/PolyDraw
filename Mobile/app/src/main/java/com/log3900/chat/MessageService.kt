package com.log3900.chat

enum class MessageEvent {
    MESSAGE_RECEIVED
}
class MessageService {
    private var subscribers: MutableMap<MessageEvent, MutableList<(Message) -> Unit>> = mutableMapOf()

    fun sendMessage(message: Message) {
        // TODO: Make call to socket service to send message.
    }

    fun subscribe(event: MessageEvent, callback: (Message) -> Unit) {
        if (subscribers[event] == null) {
            subscribers[event] = mutableListOf()
        }

        subscribers[event]?.add(callback)
    }

    fun receiveMessage(message: Message) {

    }

    private fun initialize() {
        // TODO: Make call to socket service to listen to message receiving event and pass receiveMessage function.
    }
}