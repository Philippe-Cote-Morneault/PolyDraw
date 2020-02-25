package com.log3900.chat.Channel

interface ChannelListView {
    fun setJoinedChannels(channels: ArrayList<Channel>)
    fun setAvailableChannels(channels: ArrayList<Channel>)
    fun notifyChannelSubscribed(channel: Channel)
    fun notifyChannelUnsubscried(channel: Channel)
    fun showChannelCreationDialog(positiveCallback: (channelName: String) -> Unit)
    fun hideChannelCreationDialog()
    fun notifyChannelsChange()
}