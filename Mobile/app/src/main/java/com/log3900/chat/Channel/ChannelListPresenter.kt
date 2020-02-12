package com.log3900.chat.Channel

import com.log3900.chat.ChatManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.shared.ui.ProgressDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ChannelListPresenter : Presenter {
    private var channelListView: ChannelListView
    private lateinit var chatManager: ChatManager

    constructor(channelListView: ChannelListView) {
        this.channelListView = channelListView
        if (!(ChatManager.instance?.ready!!)) {
            ChatManager.instance?.subject?.filter {
                it
            }?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                init()
            }
        } else {
            init()
        }
    }

    private fun init() {
        this.chatManager = ChatManager.instance!!
        chatManager.getJoinedChannels().observeOn(AndroidSchedulers.mainThread()).subscribe(
            { channels ->
                channelListView.setJoinedChannels(channels)
            },
            { error ->
            }
        )
        chatManager.getAvailableChannels().observeOn(AndroidSchedulers.mainThread()).subscribe(
            { channels ->
                channelListView.setAvailableChannels(channels)
            },
            { error ->
            }
        )
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.SUBSCRIBED_TO_CHANNEL -> {
                onChannelSubscribed(event.data as Channel)
            }
            EventType.UNSUBSCRIBED_FROM_CHANNEL -> {
                onChannelUnsubscribed(event.data as Channel)
            }
            EventType.CHANNEL_CREATED -> {
                onChannelCreated(event.data as Channel)
            }
            EventType.CHANNEL_DELETED -> {
                onChannelDeleted(event.data as Channel)
            }
        }
    }

    fun onChannelClicked(channel: Channel) {
        chatManager.setActiveChannel(channel)
    }

    fun onChannelActionButton1Click(channel: Channel, channelState: GroupType) {
        chatManager.changeSubscriptionStatus(channel)
    }

    private fun onChannelSubscribed(channel: Channel) {
        channelListView.notifyChannelSubscribed(channel)
    }

    private fun onChannelUnsubscribed(channel: Channel) {
        channelListView.notifyChannelUnsubscried(channel)
    }

    private fun onChannelCreated(channel: Channel) {
        channelListView.notifyChannelSubscribed(channel)
    }

    private fun onChannelDeleted(channel: Channel) {
        channelListView.notifyChannelSubscribed(channel)
    }

    override fun resume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}