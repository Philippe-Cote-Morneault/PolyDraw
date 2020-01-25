package com.log3900.socketServices

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

//import java.io.BufferedOutputStream


class SocketService{
    private val socket:Socket = Socket()
//    private lateinit var outputStream: ObjectOutputStream
    private lateinit var readSocketThread: ReadSocketThread

    fun connectSocket() {
        println("Connexion...")
        socket.connect(InetSocketAddress(InetAddress.getByName("192.168.0.99"), 4201))
        println("Connexion etablie...")

        readSocketThread = ReadSocketThread(socket)
        readSocketThread.start()
    }

    fun deconnectSocket(){
        readSocketThread.close()
    }


}