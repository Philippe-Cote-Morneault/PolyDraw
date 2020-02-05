package com.log3900

import android.app.Application
import android.content.Intent
import com.log3900.login.LoginActivity
import com.log3900.socket.SocketService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, SocketService::class.java))
    }
}