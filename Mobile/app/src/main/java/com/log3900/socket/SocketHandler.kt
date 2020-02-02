package com.log3900.socket

import android.os.Handler
import android.os.Looper
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CountDownLatch

enum class Request {
    SEND_MESSAGE,
    START_READING,
    STOP_READING,
    CONNECT,
    DISCONNECT,
    SET_MESSAGE_LISTENER
}

object SocketHandler {
    private lateinit var socket: Socket
    private lateinit var inputStream: DataInputStream
    private lateinit var outputStream: DataOutputStream
    private var requestHandler: Handler? = null
    private var messageReadListener: Handler? = null
    private var readMessages = false

    init {
        socket = Socket("10.0.2.2", 5123)
        inputStream = DataInputStream(socket.getInputStream())
        outputStream = DataOutputStream(socket.getOutputStream())
    }

    fun createRequestHandler() {
        val lock = CountDownLatch(1)
        Thread(Runnable {
            Looper.prepare()
            requestHandler = Handler {
                handleRequest(it)
                true
            }
            lock.countDown()
        }).start()
        lock.await()
    }

    fun startReadingMessages(handler: Handler) {
        messageReadListener = handler
        readMessages = true
        Thread(Runnable {
            while(readMessages) {
                readMessage()
            }
        }).start()

    }

    fun sendRequest(message: android.os.Message) {
        requestHandler?.sendMessage(message)
    }

    private fun onWriteMessage(message: Message) {
        outputStream.writeByte(message.type.eventType.toInt())
        outputStream.writeShort(message.data.size)
        outputStream.write(message.data)
    }

    fun onDisconnect() {
        socket.close()
    }

    private fun handleRequest(message: android.os.Message) {
        when (message.what) {
            Request.SEND_MESSAGE.ordinal -> {
                if (message.obj is Message) {
                    onWriteMessage(message.obj as Message)
                }
            }
            Request.START_READING.ordinal -> {

            }
            Request.STOP_READING.ordinal -> {

            }
            Request.DISCONNECT.ordinal -> {
                onDisconnect()
            }
            Request.SET_MESSAGE_LISTENER.ordinal -> {
                if (message.obj is Handler) {
                    messageReadListener = message.obj as Handler
                }
            }
        }
    }

    fun readMessage() {
        try {
            val typeByte = inputStream.readByte()
            val type = Event.values().find { it.eventType == typeByte }
                ?: throw IllegalArgumentException("Invalid message type")

            val length = inputStream.readShort()

            var values = ByteArray(length.toInt())
            inputStream.read(values)

            val message = Message(type, values)

            if (messageReadListener != null) {
                val msg = android.os.Message()
                msg.obj = message
                messageReadListener?.sendMessage(msg)
            }

        } catch (e: SocketException){
            // Gestion de la deconnexion a voir avec Samuel & Martin
            println("Connexion off")
        }
    }
}