package com.log3900.shared.architecture

enum class EventType {
    ACTIVE_CHANNEL_CHANGED,
    SUBSCRIBED_TO_CHANNEL,
    UNSUBSCRIBED_FROM_CHANNEL
}
data class MessageEvent(var type: EventType, var data: Any)