package com.log3900.chat.Channel

import com.log3900.MainApplication
import com.log3900.R
import com.log3900.chat.ChatManager
import com.log3900.chat.ChatMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ChannelListPresenter : Presenter {
    private var channelListView: ChannelListView
    private lateinit var chatManager: ChatManager

    lateinit var availableChannelsGroup: ChannelGroup
    lateinit var joinedChannelsGroup: ChannelGroup

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
                joinedChannelsGroup = ChannelGroup(GroupType.JOINED, channels, chatManager.getUnreadMessages(),  channels.clone() as ArrayList<Channel>, chatManager.getActiveChannel())
                channelListView.addChannelSection(joinedChannelsGroup)

                chatManager.getAvailableChannels().observeOn(AndroidSchedulers.mainThread()).subscribe(
                    { channels ->
                        availableChannelsGroup = ChannelGroup(GroupType.AVAILABLE, channels, chatManager.getUnreadMessages(), channels.clone() as ArrayList<Channel>)
                        channelListView.addChannelSection(availableChannelsGroup)
                    },
                    { _ ->
                    }
                )
                EventBus.getDefault().register(this)
            },
            { _ ->
            }
        )
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
            EventType.ACTIVE_CHANNEL_CHANGED -> {
                onActiveChannelChanged(event.data as Channel?)
            }
            EventType.RECEIVED_MESSAGE -> {
                onMessageReceived(event.data as ChatMessage)
            }
        }
    }

    fun onChannelClicked(channel: Channel) {
        try {
            chatManager.onChannelClicked(channel)
        } catch (e: IllegalArgumentException) {
            channelListView.showConfirmationDialog(MainApplication.instance.getContext().getString(R.string.unsubscribed_channel),
                MainApplication.instance.getContext().getString(R.string.subscribe_to_channel_confirmation_text, channel.name),
                { _, _ ->
                    chatManager.changeSubscriptionStatus(channel)
                    chatManager.setActiveChannel(channel)
                },
                { dialog, _ ->
                    dialog.dismiss()
                })
        }
    }

    fun onChannelActionButton1Click(channel: Channel, channelState: GroupType) {
        chatManager.changeSubscriptionStatus(channel)
    }

    fun onChannelActionButton2Click(channel: Channel, channelState: GroupType) {
        channelListView.showConfirmationDialog(
            MainApplication.instance.getContext().getString(R.string.delete_channel_title),
            MainApplication.instance.getContext().getString(R.string.delete_channel_confirmation, channel.name),
            { _, _ ->
                chatManager.deleteChannel(channel)
            },
            { dialog, _ ->
                dialog.dismiss()
            })
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
        channelListView.notifyChannelsChange()
    }

    private fun onChannelUnsubscribed(channel: Channel) {
        channelListView.notifyChannelsChange()
    }

    private fun onChannelCreated(channel: Channel) {
        channelListView.notifyChannelsChange()
    }

    private fun onChannelDeleted(channel: UUID) {
        channelListView.notifyChannelsChange()
    }

    private fun onActiveChannelChanged(channel: Channel?) {
        joinedChannelsGroup.activeChannel = channel
        channelListView.notifyChannelsChange()
    }

    private fun onMessageReceived(message: ChatMessage) {
        channelListView.notifyChannelsChange()
    }

    override fun resume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        EventBus.getDefault().unregister(this)

    }
}