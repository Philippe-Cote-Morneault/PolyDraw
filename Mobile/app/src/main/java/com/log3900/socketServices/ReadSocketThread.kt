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
            var typeBytes = ByteArray(1)
            inputStream.read(typeBytes, 0, 1)
            var type: Byte = ByteBuffer.wrap(typeBytes).get()
            println("Le type est $type")

            var tailleBytes: ByteArray = ByteArray(2)
            inputStream.read(tailleBytes, 0, 2)
            var taille: Short = ByteBuffer.wrap(tailleBytes).getShort()
            println("La taille est $taille")


            var messageB = ByteArray(taille.toInt())
            inputStream.read(messageB, 0, taille.toInt())

            var dataJson = JSONObject(String(messageB, Charsets.UTF_8))

            println("Avant stringify")
            println(dataJson.toString())
            println("Apres stringify")

            var message = android.os.Message()
            message.what = type as Int

            message.obj = Message(message = dataJson.get("message") as String,
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