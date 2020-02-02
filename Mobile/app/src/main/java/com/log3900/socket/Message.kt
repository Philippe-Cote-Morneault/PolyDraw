package com.log3900.socket

enum class Event(var eventType: Byte) {
    MESSAGE_RECEIVED(21),
    MESSAGE_SENT(20)
}

data class Message(var type: Event, var data: ByteArray)