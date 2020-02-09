package com.log3900.chat

import com.log3900.chat.Message.ReceivedMessage
import java.util.*

interface ChatView {
    fun openNavigationDrawer()
    fun closeNavigationDrawer()
    fun isNavigationDrawerOpened(): Boolean
    fun notifyNewMessage()
    fun setReceivedMessages(messages: LinkedList<ReceivedMessage>)
    fun setCurrentChannnelName(name: String)
    fun playNewMessageNotification()
    fun scrollMessage()
}