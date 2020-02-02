package com.log3900.socket

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

object SocketHandler {
    private var socket: Socket = Socket()
    private lateinit var inputStream: DataInputStream
    private lateinit var outputStream: DataOutputStream
}