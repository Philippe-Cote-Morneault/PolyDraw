package com.log3900.session

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import com.log3900.socket.*
import java.util.*


class MonitoringService : Service() {
    private val binder = MonitoringBinder()
    private var socketService: SocketService? = null

    companion object {
        lateinit var instance: MonitoringService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        println("monitoring service onCreate")
        instance = this
        socketService = SocketService.instance

        Thread(Runnable {
            println("creating monitoring starting")
            Looper.prepare()
            if (socketService == null) {
                println("socket service is not created")
            }
            socketService?.subscribeToEvent(SocketEvent.CONNECTION_ERROR, Handler {
                handleEvent(it)
                true
            })

            socketService?.subscribeToMessage(Event.HEALTH_CHECK_SERVER, Handler {
                handleMessage(it)
                true
            })
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketService = null
    }

    fun handleEvent(message: android.os.Message) {
        println("connection error!")
        when (message.what) {
            SocketEvent.CONNECTED.ordinal -> {

            }
        }
    }

    fun handleMessage(message: Message) {

    }

    inner class MonitoringBinder : Binder() {
        fun getService(): MonitoringService = this@MonitoringService
    }

}

