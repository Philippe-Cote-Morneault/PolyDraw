package com.log3900.chat

enum class MessageEvent {
    MESSAGE_RECEIVED
}
class MessageService {
    var subscribers: MutableMap<MessageEvent, (Message) -> Unit> = mutableMapOf()

    fun sendMessage(message: Message) {
        // TODO: Make call to socket service to send message.
    }

    private fun initialize() {
        // TODO: Make call to socket service to listen to message receiving event and pass receiveMessage function.
    }

    fun receiveMessage(message: Message) {

    }
}