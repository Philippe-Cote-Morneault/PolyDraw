package com.log3900.socket

enum class Event(var eventType: Byte) {
    SOCKET_CONNECTION(0),
    SERVER_RESPONSE(1),
    MESSAGE_RECEIVED(21),
    MESSAGE_SENT(20),
    HEALTH_CHECK_SERVER(9),
    HEALTH_CHECK_CLIENT(10)
}

data class Message(var type: Event, var data: ByteArray)