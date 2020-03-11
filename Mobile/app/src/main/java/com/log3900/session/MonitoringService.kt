package com.log3900.session

import android.app.LauncherActivity
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.log3900.MainApplication
import com.log3900.chat.Channel.ChannelRepository
import com.log3900.chat.ChatManager
import com.log3900.chat.Message.MessageRepository
import com.log3900.game.group.GroupManager
import com.log3900.game.group.GroupRepository
import com.log3900.game.match.MatchRepository
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.ui.dialogs.ErrorDialog
import com.log3900.socket.Event
import com.log3900.socket.SocketEvent
import com.log3900.socket.SocketService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MonitoringService : Service() {
    private val binder = MonitoringBinder()
    private var socketService: SocketService? = null
    private var socketEventHandler: Handler? = null
    private var socketMessageHandler: Handler? = null

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
            socketEventHandler = Handler {
                handleEvent(it)
                true
            }

            socketMessageHandler = Handler {
                handleMessage(it)
                true
            }
            socketService?.subscribeToEvent(SocketEvent.CONNECTION_ERROR, socketEventHandler!!)

            socketService?.subscribeToMessage(Event.HEALTH_CHECK_SERVER, socketMessageHandler!!)

            socketService?.subscribeToMessage(Event.SERVER_RESPONSE, socketMessageHandler!!)
            Looper.loop()
        }).start()

        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        socketService?.unsubscribeFromEvent(SocketEvent.CONNECTION_ERROR, socketEventHandler!!)
        socketService?.unsubscribeFromMessage(Event.HEALTH_CHECK_SERVER, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(Event.SERVER_RESPONSE, socketMessageHandler!!)

        socketMessageHandler = null
        socketEventHandler = null

        socketService = null
        super.onDestroy()
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
        when (message.what) {
            Event.SERVER_RESPONSE.ordinal -> {
                if ((message.obj as com.log3900.socket.Message).data[0].toInt() == 1) {
                    onAuthentication()
                }
            }
        }
    }

    fun onConnectionError() {
        shutdownServices()
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            val intent = Intent(this, ErrorDialog::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        else {
            val intent = Intent(this, LauncherActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    fun onAuthentication() {
        MainApplication.instance.startService(MessageRepository::class.java)
        MainApplication.instance.startService(ChannelRepository::class.java)
        MainApplication.instance.startService(ChatManager::class.java)

        MainApplication.instance.startService(GroupRepository::class.java)
        MainApplication.instance.startService(GroupManager::class.java)
    }

    private fun onLogout(){
        shutdownServices()
    }

    private fun onLeaveGroup() {
        GroupRepository.instance?.refreshRepository()
    }

    private fun onGroupJoined() {
        MainApplication.instance.startService(MatchRepository::class.java)
    }

    private fun onGroupLeft() {
        MainApplication.instance.stopService(MatchRepository::class.java)
    }

    private fun shutdownServices() {
        MainApplication.instance.stopService(GroupManager::class.java)
        MainApplication.instance.stopService(GroupRepository::class.java)

        MainApplication.instance.stopService(ChatManager::class.java)
        MainApplication.instance.stopService(ChannelRepository::class.java)
        MainApplication.instance.stopService(MessageRepository::class.java)
    }

    private fun restartService(service: Class<*>) {
        MainApplication.instance.stopService(service)
        MainApplication.instance.startService(service)
    }

    fun displayErro() {

    }

    @Subscribe
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.LOGOUT -> {
                onLogout()
            }
            EventType.LEAVE_GROUP -> {
                onLeaveGroup()
            }
            EventType.GROUP_JOINED -> {
                onGroupJoined()
            }
            EventType.GROUP_LEFT -> {
                onGroupLeft()
            }
        }
    }

    inner class MonitoringBinder : Binder() {
        fun getService(): MonitoringService = this@MonitoringService
    }

}

