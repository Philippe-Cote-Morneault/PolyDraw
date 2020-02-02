package com.log3900.socket

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.daveanthonythomas.moshipack.MoshiPack
import com.log3900.socketServices.MessageEvent
import com.log3900.socketServices.MessageReceived
import com.log3900.socketServices.ReadSocketThread
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

enum class Event(var eventType: Byte) {
    MESSAGE_RECEIVED(21),
    MESSAGE_SENT(20)
}

class SocketService : Service() {
    private var socket: Socket = Socket()
    private lateinit var inputStream: DataInputStream
    private lateinit var outputStream: DataOutputStream
    private var subscribers: ConcurrentHashMap<Event, ArrayList<Handler>> = ConcurrentHashMap()
    private var listenToMessages = true

    fun disconnect() {
        socket.close()
    }

    fun sendMessage(event: Event, data: ByteArray) {
        outputStream.writeByte(event.eventType.toInt())
        outputStream.writeShort(data.size)
        outputStream.write(data)
    }

    fun sendSerializedMessage(event: Event, data: Any) {
        val serializedMessage = MoshiPack().packToByteArray(data)
        outputStream.writeByte(event.eventType.toInt())
        outputStream.writeShort(serializedMessage.size)
        outputStream.write(serializedMessage)
    }

    fun subscribe(event: Event, handler: Handler) {
        if (!subscribers.containsKey(event)) {
            subscribers[event] = ArrayList()
        }

        subscribers[event]?.add(handler)
    }

    fun startListening() {

    }

    fun stopListening() {

    }

    private fun listenToMessages() {
        while (listenToMessages) {
           val message = readMessage()
            if (message != null) {
                notifySubscribers(message)
            }
        }
    }

    fun readMessage(): Message? {
            try {
                val typeByte = inputStream.readByte()
                val type = Event.values().find { it.eventType == typeByte }
                    ?: throw IllegalArgumentException("Invalid message type")

                println("Le type est " + type)

                val length = inputStream.readShort()
                println("length = " + length.toInt())

                var values = ByteArray(length.toInt())
                inputStream.read(values)

                return Message(type, values)

            } catch (e: SocketException){
                // Gestion de la deconnexion a voir avec Samuel & Martin
                println("Connexion off")
                return null
            }
    }

    private fun notifySubscribers(message: Message){
        val handlerMessage = android.os.Message()
        handlerMessage.what = message.type.eventType.toInt()
        handlerMessage.obj = message.data

        if (subscribers.containsKey(message.type)) {
            val handlers = subscribers[message.type]
            for (handler in handlers.orEmpty()) {
                handler.sendMessage(handlerMessage)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        println("inside onCreate")
        println("inside init")
        //socket = Socket()
        //println("Connexion...")
        //socket.connect(InetSocketAddress(InetAddress.getByName("192.168.0.99"), 4201))
        //println("Connexion etablie...")
        // outputStream = DataOutputStream(socket.getOutputStream())
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

}