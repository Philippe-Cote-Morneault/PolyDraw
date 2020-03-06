package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.user.account.AccountRepository
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class GroupManager : Service() {
    private val binder = GroupManagerBinder()
    private var groupRepository: GroupRepository? = null
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
            }
        )
    }

    fun joinGroup(group: Group) {
        groupRepository?.joinGroup(group.ID)
    }

    fun leaveCurrentGroup() {
        if (currentGroup != null) {
            groupRepository?.leaveGroup(currentGroup!!.ID)
        }
    }

    private fun onGroupJoined(group: Group) {
        currentGroup = group
    }

    private fun onGroupLeft(groupID: UUID) {
        currentGroup = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        groupRepository = GroupRepository.instance

        EventBus.getDefault().register(this)

        setReadyState()
    }

    private fun setReadyState() {
        isReady = true
        isReadySignal.onNext(true)
    }

    override fun onDestroy() {
        instance = null
        isReady = false
        groupRepository = null
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
                onGroupLeft(event.data as UUID)
            }
        }
    }

    inner class GroupManagerBinder : Binder() {
        fun getService(): GroupManager = this@GroupManager
    }
}