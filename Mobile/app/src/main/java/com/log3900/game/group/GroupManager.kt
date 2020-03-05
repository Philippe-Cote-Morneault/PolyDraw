package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.log3900.user.account.AccountRepository
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class GroupManager : Service() {
    private val binder = GroupManagerBinder()
    private var groupRepository: GroupRepository? = null

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

    fun leaveGroup(group: Group) {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        groupRepository = GroupRepository.instance

        setReadyState()
    }

    private fun setReadyState() {
        isReady = true
        isReadySignal.onNext(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isReady = false
        groupRepository = null
    }

    inner class GroupManagerBinder : Binder() {
        fun getService(): GroupManager = this@GroupManager
    }
}