package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.log3900.socket.SocketService

class GroupRepository : Service() {
    private val binder = GroupRepositoryBinder()
    private var socketService: SocketService? = null
    private var socketMessageHandler: Handler? = null
    private lateinit var groupCache: GroupCache

    var isReady = false

    companion object {
        var instance: GroupRepository? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        

        Thread(Runnable {
            Looper.prepare()
            Looper.loop()
        }).start()
    }

    override fun onDestroy() {
        socketService = null
        instance = null
        super.onDestroy()
    }

    inner class GroupRepositoryBinder : Binder() {
        fun getService(): GroupRepository = this@GroupRepository
    }
}