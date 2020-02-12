package com.log3900.chat.Channel

import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.Account
import com.log3900.user.AccountRepository
import org.greenrobot.eventbus.EventBus

class ChannelManager {
    private var account: Account
    lateinit var activeChannel: Channel
    lateinit var availableChannels: ArrayList<Channel>
    lateinit var joinedChannels: ArrayList<Channel>

    constructor() {
        account = AccountRepository.getAccount()
    }

    fun init() {
        joinedChannels = ChannelRepository.instance?.getJoinedChannels(account.sessionToken)?.blockingGet()!!
        availableChannels = ChannelRepository.instance?.getAvailableChannels(account.sessionToken)?.blockingGet()!!
        activeChannel = joinedChannels.find {
            it.ID.toString() == "00000000-0000-0000-0000-000000000000"
        }!!
    }

    fun changeSubscriptionStatus(channel: Channel) {
        var changeToGeneral = false
        if (channel.ID.toString() == "00000000-0000-0000-0000-000000000000") {
            ChannelRepository.instance?.createChannel("BLyat")
            return
        }

        if (activeChannel == channel) {
            changeToGeneral = true
        }

        if (availableChannels.contains(channel)) {
            ChannelRepository.instance?.subscribeToChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.SUBSCRIBED_TO_CHANNEL, channel))
        } else if (joinedChannels.contains(channel)){
            ChannelRepository.instance?.unsubscribeFromChannel(channel)
            EventBus.getDefault().post(MessageEvent(EventType.UNSUBSCRIBED_FROM_CHANNEL, channel))
        } else {
            // TODO: Handle this incoherent state
        }

        if (changeToGeneral) {
            activeChannel = joinedChannels.find {
                it.ID.toString() == "00000000-0000-0000-0000-000000000000"
            }!!
            EventBus.getDefault().post(MessageEvent(EventType.ACTIVE_CHANNEL_CHANGED, channel))
        }
    }

    fun createChannel(channelName: String) {
        ChannelRepository.instance?.createChannel(channelName)
    }


}