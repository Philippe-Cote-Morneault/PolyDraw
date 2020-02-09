package com.log3900.shared.architecture

enum class EventType {
    CHANGED_CHANNEL
}
data class MessageEvent(var type: EventType, var data: Any)