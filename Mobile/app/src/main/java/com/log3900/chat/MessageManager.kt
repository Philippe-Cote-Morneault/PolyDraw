package com.log3900.chat

class MessageManager {
    private var messageRepository: MessageRepository

    constructor() {
        messageRepository = MessageRepository.instance!!
    }
}