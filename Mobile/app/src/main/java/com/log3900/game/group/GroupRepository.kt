package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.ArrayListUUIDAdapter
import com.log3900.utils.format.moshi.GroupAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

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
                        .add(ArrayListUUIDAdapter())
                        .add(GroupAdapter())
                        .build()

                    val groups = arrayListOf<Group>()

                    response.body()?.forEach { group ->
                        groups.add(GroupAdapter().fromJson(group.asJsonObject))
                    }
                    it.onSuccess(groups)
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }
    
    fun getGroup(sessionToken: String, groupID: UUID): Single<Group> {
        return Single.create {
            val call = GroupRestService.service.getGroup(sessionToken, "EN", groupID.toString())
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    it.onSuccess(GroupAdapter().fromJson(response.body()!!.asJsonObject))
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }

    fun createGroup(sessionToken: String, group: GroupCreated): Single<UUID> {
        return Single.create {
            val call = GroupRestService.service.createGroup(sessionToken, "EN", group.toJsonObject())
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    when (response.code()) {
                        200 -> {
                            val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(UUIDAdapter())
                                .build()

                            val adapter: JsonAdapter<UUID> = moshi.adapter(UUID::class.java)
                            val res = adapter.fromJson(response.body()!!.getAsJsonPrimitive("GroupID").toString())
                            it.onSuccess(res as UUID)
                        }
                        else -> {
                            it.onError(Exception())
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
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