package com.log3900.chat.Channel

import com.log3900.chat.ChatManager
import com.log3900.shared.architecture.Presenter
import com.log3900.shared.ui.ProgressDialog
import io.reactivex.android.schedulers.AndroidSchedulers

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
    }

    fun onChannelClicked(channel: Channel) {
        chatManager.setActiveChannel(channel)
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