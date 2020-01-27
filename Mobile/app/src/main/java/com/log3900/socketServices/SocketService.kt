package com.log3900.socketServices

import android.os.Handler
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.io.BufferedOutputStream


class SocketService{
    private var socket:Socket = Socket()
    private lateinit var outputStream: BufferedOutputStream
    private lateinit var readSocketThread: ReadSocketThread

    fun connectSocket() {
        println("Connexion...")
        socket.connect(InetSocketAddress(InetAddress.getByName("192.168.0.99"), 4201))
        println("Connexion etablie...")
        outputStream = BufferedOutputStream(socket.getOutputStream())
        readSocketThread = ReadSocketThread(BufferedInputStream(socket.getInputStream()))
        readSocketThread.start()
    }

    fun deconnectSocket(){
        readSocketThread.close()
    }

    fun subscribe(event:MessageEvent, handler:Handler){
        if (!readSocketThread.subscribers.containsKey(event)) {
            readSocketThread.subscribers[event] = ArrayList()
        }

        readSocketThread.subscribers[event]?.add(handler)
    }

    fun sendMessage(message:MessageSent){
        var dataJson = JSONObject()
        dataJson.put("message", message.message)
        dataJson.put("channelID", message.channelID)

        var values = dataJson.toString().toByteArray()

        outputStream.write(values.size)
        outputStream.write(values)
        outputStream.flush()

    }
}