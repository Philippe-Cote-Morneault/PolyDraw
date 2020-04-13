package com.log3900.chat.Channel

import android.content.DialogInterface

interface ChannelListView {
    fun setJoinedChannels(channels: ArrayList<Channel>)
    fun setAvailableChannels(channels: ArrayList<Channel>)
    fun notifyChannelSubscribed(channel: Channel)
    fun notifyChannelUnsubscried(channel: Channel)
    fun showChannelCreationDialog(positiveCallback: (channelName: String) -> Unit)
    fun hideChannelCreationDialog()
    fun showConfirmationDialog(title: String, message: String, positiveButtonListener: ((dialog: DialogInterface, which: Int) -> Unit),
                               negativeButtonListener: ((dialog: DialogInterface, which: Int) -> Unit))
    fun hideConfirmationDialog()
    fun notifyChannelsChange()
    fun changeActiveChannel(channel: Channel)
    fun changeChanngelUnreadMessages(channel: Channel, unreadMessages: Int)
    fun addChannelSection(channelGroup: ChannelGroup)
}