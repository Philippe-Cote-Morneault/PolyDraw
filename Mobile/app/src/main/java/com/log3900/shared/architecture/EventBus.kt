package com.log3900.shared.architecture

enum class EventType {
    // Channel
    ACTIVE_CHANNEL_CHANGED,
    ACTIVE_CHANNEL_MESSAGE_RECEIVED,
    SUBSCRIBED_TO_CHANNEL,
    UNSUBSCRIBED_FROM_CHANNEL,
    CHANNEL_CREATED,
    CHANNEL_DELETED,
    // Message
    RECEIVED_MESSAGE,
    UNREAD_MESSAGES_CHANGED,
    // Group
    GROUP_CREATED,
    GROUP_DELETED,
    GROUP_UPDATED,
    // Session
    LOGOUT,

}
data class MessageEvent(var type: EventType, var data: Any?)