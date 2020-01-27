package com.log3900.socketServices

import android.os.Handler
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class ReadSocketThread(var inputStream: BufferedInputStream): Thread() {
    var subscribers: ConcurrentHashMap<MessageEvent, ArrayList<Handler>> = ConcurrentHashMap()

    override fun run(){
        println("Pret a recevoir...")
        while (true){
            var message: android.os.Message? = readMessage()
            if(message != null){
                notifySubscribers(message)
            }


        }
    }

    fun close() {
        inputStream.close()
    }

    private fun notifySubscribers(message:android.os.Message){
        for (subscriber in subscribers){
            if(subscriber.key.eventType == message.what) {
                subscriber.value.forEach { it.sendMessage(message) }
                return
            }
        }
    }

    private fun readMessage() : android.os.Message?{

        try {
            var type = ByteArray(1)
            inputStream.read(type)
            println("Le type est " + ByteBuffer.wrap(type).get().toString())

            var length = ByteArray(2)
            inputStream.read(length)
            println(ByteBuffer.wrap(length).getShort().toInt())

            var values = ByteArray(ByteBuffer.wrap(length).getShort().toInt())
            inputStream.read(values)

            var dataJson = JSONObject(String(values, Charsets.UTF_8))

            println("Avant stringify")
            println(dataJson.toString())
            println("Apres stringify")

            var message = android.os.Message()
            message.what = ByteBuffer.wrap(type).get() as Int

            message.obj = MessageReceived(message = dataJson.get("message") as String,
                channelID = dataJson.get("channelID") as UUID,
                senderID = dataJson.get("senderID") as UUID,
                senderName = dataJson.get("senderName") as String,
                timestamp = dataJson.get("timestamp") as Date)

            return message

        } catch (e:SocketException){
            // Gestion de la deconnexion a voir avec Samuel & Martin
            println("Connexion off")
            return null
        }
    }
}