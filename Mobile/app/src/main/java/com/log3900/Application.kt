package com.log3900

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.log3900.chat.MessageRepository
import com.log3900.login.LoginActivity
import com.log3900.session.MonitoringService
import com.log3900.socket.SocketService
import java.net.Socket

class MainApplication : Application() {
    companion object {
        lateinit var instance: MainApplication
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        val serviceConnection1 = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }

        bindService(Intent(this, SocketService::class.java), serviceConnection1, Context.BIND_AUTO_CREATE)

        bindService(Intent(this, MonitoringService::class.java), serviceConnection1, Context.BIND_AUTO_CREATE)

        bindService(Intent(this, MessageRepository::class.java), serviceConnection1, Context.BIND_AUTO_CREATE)
    }
}