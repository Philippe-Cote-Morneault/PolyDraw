package com.log3900.chat

interface ChatView {
    fun prependMessage(message: ReceivedMessage)
    fun appendMessage(message: ReceivedMessage)
}