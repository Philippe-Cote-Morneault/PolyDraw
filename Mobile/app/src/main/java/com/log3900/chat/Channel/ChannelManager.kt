package com.log3900.chat.Channel

import com.log3900.chat.ChatManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.User
import com.log3900.user.UserRepository
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class ChannelManager {
    private var user: User
    lateinit var activeChannel: Channel
    lateinit var availableChannels: ArrayList<Channel>
    lateinit var joinedChannels: ArrayList<Channel>

    constructor() {
        user = UserRepository.getUser()
    }

    fun init() {
        joinedChannels = ChannelRepository.instance?.getJoinedChannels(user.sessionToken)?.blockingGet()!!
        availableChannels = ChannelRepository.instance?.getAvailableChannels(user.sessionToken)?.blockingGet()!!
        activeChannel = joinedChannels.find {
            it.ID.toString() == "00000000-0000-0000-0000-000000000000"
        }!!
        EventBus.getDefault().register(this)
    }

    fun changeSubscriptionStatus(channel: Channel) {
        var changeToGeneral = false
        if (channel.ID.toString() == "00000000-0000-0000-0000-000000000000") {
            return
        }

        if (availableChannels.contains(channel)) {
            ChannelRepository.instance?.subscribeToChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.SUBSCRIBED_TO_CHANNEL, channel))
        } else if (joinedChannels.contains(channel)){
            if (activeChannel == channel) {
                changeToGeneral = true
            }
            ChannelRepository.instance?.unsubscribeFromChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.UNSUBSCRIBED_FROM_CHANNEL, channel))
        } else {
            // TODO: Handle this incoherent state
        }

        if (changeToGeneral) {
            activeChannel = joinedChannels.find {
                it.ID.toString() == "00000000-0000-0000-0000-000000000000"
            }!!
            EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_CHANGED, activeChannel))
        }
    }

    fun createChannel(channelName: String): Boolean {
        var foundChannel: Channel? = joinedChannels.find { it.name == channelName }
        if (foundChannel != null) {
            return false
        }

        foundChannel = availableChannels.find { it.name == channelName }

        if (foundChannel != null) {
            return false
        }

        ChannelRepository.instance?.createChannel(channelName)

        return true
    }

    fun deleteChannel(channel: Channel) {
        ChannelRepository.instance?.deleteChannel(channel)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.CHANNEL_CREATED -> {
                onChannelCreated(event.data as Channel)
            }
            EventType.CHANNEL_DELETED -> {
                onChannelDeleted(event.data as UUID)
            }
        }
    }

    fun onChannelCreated(channel: Channel) {
        if (channel.users.get(0).name == user.username) {
            changeSubscriptionStatus(channel)
            activeChannel = channel
            EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_CHANGED, channel))
        }
    }

    fun onChannelDeleted(channelID: UUID) {
        if (activeChannel.ID == channelID) {
            activeChannel = joinedChannels.find {
                it.ID.toString() == "00000000-0000-0000-0000-000000000000"
            }!!
            EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_CHANGED, activeChannel))
        }
    }


}