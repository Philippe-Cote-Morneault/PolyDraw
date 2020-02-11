package com.log3900.shared.architecture

enum class EventType {
    ACTIVE_CHANNEL_CHANGED
}
data class MessageEvent(var type: EventType, var data: Any)