package com.log3900.chat.Channel

import android.util.Log
import com.log3900.chat.ChatMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChannelManager {
    lateinit var activeChannel: Channel
    lateinit var availableChannels: ArrayList<Channel>
    lateinit var joinedChannels: ArrayList<Channel>
    var unreadMessages: HashMap<UUID, Int> = hashMapOf()
    var unreadMessagesTotal: Int = 0

    constructor() {
    }

    fun init() {
        joinedChannels = ChannelRepository.instance?.getJoinedChannels(AccountRepository.getInstance().getAccount().sessionToken)?.blockingGet()!!
        availableChannels = ChannelRepository.instance?.getAvailableChannels(AccountRepository.getInstance().getAccount().sessionToken)?.blockingGet()!!
        activeChannel = joinedChannels.find {
            it.ID.toString() == "00000000-0000-0000-0000-000000000000"
        }!!
        EventBus.getDefault().register(this)
    }

    fun changeSubscriptionStatus(channel: Channel) {
        if (channel.ID.toString() == "00000000-0000-0000-0000-000000000000") {
            return
        }

        if (availableChannels.contains(channel)) {
            ChannelRepository.instance?.subscribeToChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.SUBSCRIBED_TO_CHANNEL, channel))
        } else if (joinedChannels.contains(channel)){
            if (activeChannel == channel) {
                val newActiveChannel = joinedChannels.find {
                    it.ID.toString() == "00000000-0000-0000-0000-000000000000"
                }!!
                changeActiveChannel(newActiveChannel)
            }
            ChannelRepository.instance?.unsubscribeFromChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.UNSUBSCRIBED_FROM_CHANNEL, channel))
        } else {
            // TODO: Handle this incoherent state
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
            EventType.RECEIVED_MESSAGE -> {
                onMessageReceived(event.data as ChatMessage)
            }
        }
    }

    fun onChannelCreated(channel: Channel) {
        if (channel.users.get(0).ID == AccountRepository.getInstance().getAccount().ID) {
            changeSubscriptionStatus(channel)
            changeActiveChannel(channel)
        }
    }

    fun onChannelDeleted(channelID: UUID) {
        if (activeChannel.ID == channelID) {
            val newActiveChannel = joinedChannels.find {
                it.ID.toString() == "00000000-0000-0000-0000-000000000000"
            }!!
            changeActiveChannel(newActiveChannel)
        }

        if (unreadMessages.containsKey(channelID)) {
            unreadMessagesTotal -= unreadMessages.get(channelID)!!
            unreadMessages.remove(channelID)
            EventBus.getDefault().post(MessageEvent(EventType.UNREAD_MESSAGES_CHANGED, unreadMessagesTotal))
        }
    }

    private fun changeActiveChannel(channel: Channel) {
        activeChannel = channel
        if (unreadMessages.containsKey(channel.ID)) {
            unreadMessagesTotal -= unreadMessages.get(channel.ID)!!
            unreadMessages[channel.ID] = 0
        }
        EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_CHANGED, activeChannel))
    }

    private fun onMessageReceived(message: ChatMessage) {
        if (message.channelID != activeChannel.ID) {
            if (!unreadMessages.containsKey(message.channelID)) {
                unreadMessages.put(message.channelID, 0)
            }

            unreadMessages[message.channelID]?.plus(1)
            unreadMessagesTotal += 1
            EventBus.getDefault().post(MessageEvent(EventType.UNREAD_MESSAGES_CHANGED, unreadMessagesTotal))
        } else {
            EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_MESSAGE_RECEIVED, message))
        }
    }


}