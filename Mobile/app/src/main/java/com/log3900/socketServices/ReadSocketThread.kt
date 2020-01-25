package com.log3900.socketServices

import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer


class ReadSocketThread(val socket: Socket): Thread() {
    private lateinit var inputStream: BufferedInputStream

    override fun run(){
        println("Pret a recevoir...")
        inputStream = BufferedInputStream(socket.getInputStream())
        var typeBytes:ByteArray = ByteArray(1)

        while (true){
            try {
                inputStream.read(typeBytes, 0, 1)
                var type: Byte = ByteBuffer.wrap(typeBytes).get()
                println("Le type est $type")

                var tailleBytes: ByteArray = ByteArray(2)
                inputStream.read(tailleBytes, 0, 2)
                var taille: Short = ByteBuffer.wrap(tailleBytes).getShort()
                println("La taille est $taille")


                var messageB: ByteArray = ByteArray(taille.toInt())
                inputStream.read(messageB, 0, taille.toInt())

                val dataJson = JSONObject(String(messageB, Charsets.UTF_8))


                println("Avant stringify")
                println(dataJson.toString())
                println("Apres stringify")
            } catch (e:SocketException){
                println("Connexion off")
                return
            }
        }



    }

    fun close() {
        inputStream.close()
    }
}