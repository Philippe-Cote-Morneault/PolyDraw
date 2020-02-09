package com.log3900.chat

import android.os.Handler
import android.os.Message
import com.log3900.shared.architecture.Presenter
import java.util.*

class ChatPresenter : Presenter {
    private var chatView: ChatView
    private var messageRepository: MessageRepository

    constructor(chatView: ChatView) {
        this.chatView = chatView
        messageRepository = MessageRepository.instance!!

        subscribeToEvents()
    }

    fun sendMessage(message: SentMessage) {
        messageRepository.sendMessage(message)
    }

    fun sendMessage(messageText: String) {
        val message = SentMessage(messageText, UUID.randomUUID())
        sendMessage(message)
    }

    private fun subscribeToEvents() {
        messageRepository.subscribe(MessageRepository.Event.MESSAGE_RECEIVED, Handler {
            handleNewMessage(it)
            true
        })
    }

    private fun handleNewMessage(message: Message) {
        chatView.appendMessage(message.obj as ReceivedMessage)
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