package com.log3900.chat

import androidx.fragment.app.DialogFragment
import java.util.*

interface ChatView {
    fun openNavigationDrawer()
    fun closeNavigationDrawer()
    fun isNavigationDrawerOpened(): Boolean
    fun notifyNewMessage()
    fun setChatMessages(messages: LinkedList<ChatMessage>)
    fun setCurrentChannnelName(name: String)
    fun scrollMessage(smooth: Boolean)
    fun showProgressDialog(dialog: DialogFragment)
    fun hideProgressDialog(dialog: DialogFragment)
    fun notifyMessagesPrepended(count: Int)
    fun notifyMessagesUpdated(userChanged: UUID)
}