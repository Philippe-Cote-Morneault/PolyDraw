package com.log3900

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.log3900.chat.Channel.ChannelRepository
import com.log3900.chat.ChatManager
import com.log3900.chat.Message.MessageRepository
import com.log3900.session.MonitoringService
import com.log3900.socket.SocketService

class MainApplication : Application() {
    companion object {
        lateinit var instance: MainApplication
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        startService(SocketService::class.java)
        startService(MonitoringService::class.java)
    }

    fun startService(service: Class<*>) {
        startService(Intent(this, service))
    }

    fun stopService(service: Class<*>) {
        stopService(Intent(this, service))
    }
}