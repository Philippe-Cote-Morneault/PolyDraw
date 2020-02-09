package com.log3900.session

import android.app.AlertDialog
import android.app.LauncherActivity
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.log3900.MainActivity
import com.log3900.shared.ui.WarningDialog
import com.log3900.socket.*
import java.util.*


class MonitoringService : Service() {
    private val binder = MonitoringBinder()
    private var socketService: SocketService? = null

    companion object {
        var instance: MonitoringService? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        socketService = SocketService.instance

        Thread(Runnable {
            Looper.prepare()
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
        when (message.what) {
            SocketEvent.CONNECTED.ordinal -> {
                onConnectionError()
            }
            SocketEvent.CONNECTION_ERROR.ordinal -> {
                onConnectionError()
            }
        }
    }

    fun handleMessage(message: Message) {

    }

    fun onConnectionError() {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            val intent = Intent(this, WarningDialog::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        else {
            val intent = Intent(this, LauncherActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    fun displayErro() {

    }

    inner class MonitoringBinder : Binder() {
        fun getService(): MonitoringService = this@MonitoringService
    }

}

