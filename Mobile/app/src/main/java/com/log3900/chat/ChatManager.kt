package com.log3900.chat

import com.log3900.chat.Channel.ChannelManager
import com.log3900.chat.Message.MessageManager
import com.log3900.chat.Message.MessageRepository
import com.log3900.chat.Message.ReceivedMessage
import io.reactivex.Observable
import java.util.*

class ChatManager {
    private var channelManager: ChannelManager
    private var messageManager: MessageManager

    constructor() {
        channelManager = ChannelManager()
        messageManager = MessageManager()
    }

    fun getCurrentChannelMessage(): LinkedList<ReceivedMessage> {
        return MessageRepository.instance?.getChannelMessages("", "", 0 ,0)!!
    }

    fun test() = Observable.just(MessageRepository.instance?.getChannelMessages("", "", 0, 0))


}