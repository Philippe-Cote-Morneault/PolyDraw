package com.log3900.chat.Channel

interface ChannelListView {
    //fun showChannel
    fun setJoinedChannels(channels: ArrayList<Channel>)
    fun setAvailableChannels(channels: ArrayList<Channel>)
    fun notifyChannelSubscribed(channel: Channel)
    fun notifyChannelUnsubscried(channel: Channel)
}