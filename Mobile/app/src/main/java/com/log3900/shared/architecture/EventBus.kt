package com.log3900.shared.architecture

enum class EventType {
    ACTIVE_CHANNEL_CHANGED,
    ACTIVE_CHANNEL_MESSAGE_RECEIVED,
    SUBSCRIBED_TO_CHANNEL,
    UNSUBSCRIBED_FROM_CHANNEL,
    CHANNEL_CREATED,
    CHANNEL_DELETED,
    RECEIVED_MESSAGE,
    UNREAD_MESSAGES_CHANGED,
    LOGOUT,

}
data class MessageEvent(var type: EventType, var data: Any)