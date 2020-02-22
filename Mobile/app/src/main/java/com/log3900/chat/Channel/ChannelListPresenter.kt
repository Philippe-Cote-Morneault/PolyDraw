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
import java.util.*

class ChannelListPresenter : Presenter {
    private var channelListView: ChannelListView
    private lateinit var chatManager: ChatManager

    constructor(channelListView: ChannelListView) {
        this.channelListView = channelListView
        ChatManager.getInstance()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            {
                chatManager = it
                init()
            },
            {

            }
        )
    }

    private fun init() {
        chatManager.getJoinedChannels().observeOn(AndroidSchedulers.mainThread()).subscribe(
            { channels ->
                channelListView.setJoinedChannels(channels)
            },
            { _ ->
            }
        )
        chatManager.getAvailableChannels().observeOn(AndroidSchedulers.mainThread()).subscribe(
            { channels ->
                channelListView.setAvailableChannels(channels)
            },
            { _ ->
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
                onChannelDeleted(event.data as UUID)
            }
        }
    }

    fun onChannelClicked(channel: Channel) {
        chatManager.setActiveChannel(channel)
    }

    fun onChannelActionButton1Click(channel: Channel, channelState: GroupType) {
        chatManager.changeSubscriptionStatus(channel)
    }

    fun onChannelActionButton2Click(channel: Channel, channelState: GroupType) {
        chatManager.deleteChannel(channel)
    }

    fun onCreateChannelButtonClick() {
        channelListView.showChannelCreationDialog {
            chatManager.createChannel(it).observeOn(AndroidSchedulers.mainThread()).subscribe(
                {

                },
                {

                }
            )
        }
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

    private fun onChannelDeleted(channel: UUID) {
        channelListView.notifyChannelsChange()
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