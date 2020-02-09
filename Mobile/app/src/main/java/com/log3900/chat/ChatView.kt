package com.log3900.chat

interface ChatView {
    fun prependMessage(message: ReceivedMessage)
    fun appendMessage(message: ReceivedMessage)
    fun openNavigationDrawer()
    fun closeNavigationDrawer()
    fun isNavigationDrawerOpened(): Boolean
}