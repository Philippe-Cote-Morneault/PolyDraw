package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.shared.architecture.DialogEventMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.ui.dialogs.SimpleConfirmationDialog
import com.log3900.shared.ui.dialogs.SimpleErrorDialog
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class GroupManager : Service() {
    private val binder = GroupManagerBinder()
    private var groupRepository: GroupRepository? = null
    private var socketService: SocketService? = null
    private var socketMessageHandler: Handler? = null
    var currentGroup: Group? = null

    companion object {
        private var isReady = false
        private var isReadySignal: PublishSubject<Boolean> = PublishSubject.create()
        private var instance: GroupManager? = null

        fun getInstance(): Single<GroupManager> {
            return Single.create {
                val readySignal = isReadySignal.subscribe(
                    { _ ->
                        it.onSuccess(instance!!)
                    },
                    {

                    }
                )
                if (isReady) {
                    readySignal.dispose()
                    it.onSuccess(instance!!)
                }
            }
        }

    }

    fun getAvailableGroups(): Single<ArrayList<Group>> {
        return groupRepository?.getGroups(AccountRepository.getInstance().getAccount().sessionToken)!!
    }

    fun createGroup(createdGroup: GroupCreated) {
        groupRepository?.createGroup(AccountRepository.getInstance().getAccount().sessionToken, createdGroup)?.subscribe(
            {
            },
            {
                EventBus.getDefault().post(MessageEvent(EventType.SHOW_ERROR_MESSAGE, DialogEventMessage("Error", it.message!!, null, null)))
            }
        )
    }

    fun joinGroup(group: Group) {
        groupRepository?.joinGroup(group.ID)
    }

    fun leaveCurrentGroup() {
        EventBus.getDefault().post(MessageEvent(EventType.LEAVE_GROUP, null))
        if (currentGroup != null) {
            groupRepository?.leaveGroup(currentGroup!!.ID)
        }
    }

    fun kickPlayer(player: Player) {
        groupRepository?.kickPlayer(player)
    }

    fun addVirtualPlayers(amountOfPlayers: Byte) {
        groupRepository?.addVirtualPlayers(amountOfPlayers)
    }

    fun startMatch() {
        socketService?.sendMessage(Event.START_MATCH, byteArrayOf())
    }

    private fun onGroupJoined(group: Group) {
        currentGroup = group
    }

    private fun onPlayerLeftGroup(userLeftGroup: UserLeftGroup) {
        if (currentGroup != null && userLeftGroup.groupID == currentGroup!!.ID) {
            if (userLeftGroup.userID == currentGroup!!.ownerID) {
                EventBus.getDefault().post(MessageEvent(EventType.SHOW_ERROR_MESSAGE, DialogEventMessage(
                    MainApplication.instance.getContext().getString(R.string.warning),
                    MainApplication.instance.getContext().getString(R.string.host_left_group_dialog_message),
                    null,
                    null
                )))
            }
        }
    }

    private fun onGroupLeft(userLeftGroup: UserLeftGroup) {
        currentGroup = null
    }

    private fun onMatchAboutToStart() {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        groupRepository = GroupRepository.instance
        socketService = SocketService.instance

        socketMessageHandler = Handler {
            handleSocketMessage(it)
            true
        }

        socketService?.subscribeToMessage(Event.MATCH_ABOUT_TO_START, socketMessageHandler!!)

        EventBus.getDefault().register(this)

        setReadyState()
    }

    private fun handleSocketMessage(message: Message) {
        val socketMessage = message.obj as com.log3900.socket.Message

        when (socketMessage.type) {
            Event.MATCH_ABOUT_TO_START -> onMatchAboutToStart()
        }
    }

    private fun setReadyState() {
        isReady = true
        isReadySignal.onNext(true)
    }

    override fun onDestroy() {
        instance = null
        isReady = false
        groupRepository = null
        socketService?.unsubscribeFromMessage(Event.MATCH_ABOUT_TO_START, socketMessageHandler!!)
        socketService = null
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.GROUP_JOINED -> {
                onGroupJoined(event.data as Group)
            }
            EventType.GROUP_LEFT -> {
                onGroupLeft(event.data as UserLeftGroup)
            }
            EventType.PLAYER_LEFT_GROUP -> {
                onPlayerLeftGroup(event.data as UserLeftGroup)
            }
        }
    }

    inner class GroupManagerBinder : Binder() {
        fun getService(): GroupManager = this@GroupManager
    }
}