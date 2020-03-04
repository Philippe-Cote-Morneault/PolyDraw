package com.log3900.game.lobby

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.log3900.chat.ChatManager
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class GroupManager : Service() {
    private val binder = GroupManagerBinder()

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

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

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
    }

    inner class GroupManagerBinder : Binder() {
        fun getService(): GroupManager = this@GroupManager
    }
}