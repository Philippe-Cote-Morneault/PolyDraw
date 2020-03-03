package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.google.gson.JsonArray
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupRepository : Service() {
    private val binder = GroupRepositoryBinder()
    private var socketService: SocketService? = null
    private var socketMessageHandler: Handler? = null
    private lateinit var groupCache: GroupCache

    var isReady = false

    companion object {
        var instance: GroupRepository? = null
    }

    private fun initializeRepository() {
        instance = this
        socketService = SocketService.instance
        groupCache = GroupCache()
        getGroups(AccountRepository.getInstance().getAccount().sessionToken).subscribe(
            {
                println("Groups = ")
                it.forEach {group ->
                    println(group.toString())
                }
                isReady = true
            },
            {

            }
        )
    }

    fun getGroups(sessionToken: String): Single<ArrayList<Group>> {
        return Single.create {
            val call = GroupRestService.service.getGroups(sessionToken, "EN")
            call.enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .add(UUIDAdapter())
                        .build()

                    val adapter: JsonAdapter<List<Group>> = moshi.adapter(Types.newParameterizedType(List::class.java, Group::class.java))
                    val res = adapter.fromJson(response.body().toString())
                    it.onSuccess(res as ArrayList<Group>)
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    println("onFailure")
                }
            })
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        initializeRepository()

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