package com.log3900.user

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.log3900.chat.Message.MessageRepository
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.MatchAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.log3900.utils.format.moshi.UserAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

class UserRepository  : Service() {
    private val binder = UserRepositoryBinder()
    private var socketService: SocketService? = null

    private var userCache: UserCache = UserCache()
    private var ongoingRequests: HashMap<UUID, Single<User>> = HashMap()
    private var socketMessageHandler: Handler? = null

    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            return instance!!
        }
    }

    fun getUser(userID: UUID): Single<User> {
        if (userCache.containsUser(userID)) {
            return Single.create {
                it.onSuccess(getUserFromCache(userID))
            }
        } else if (ongoingRequests.containsKey(userID)) {
            return ongoingRequests[userID]!!
        } else {
            val request =  Single.create<User> {
                getUserFromRest(userID).subscribe(
                    { user ->
                        if (!userCache.containsUser(userID)) {
                            userCache.addUser(user)
                        }
                        ongoingRequests.remove(userID)
                        it.onSuccess(userCache.getUser(userID))
                    },
                    {
                    }
                )
            }.cache()

            ongoingRequests[userID] = request

            return request
        }
    }

    private fun getUserFromCache(userID: UUID): User {
        return userCache.getUser(userID)
    }

    private fun getUserFromRest(userID: UUID): Single<User> {
        return Single.create {
            val call = UserRestService.service.getUser(AccountRepository.getInstance().getAccount().sessionToken, LanguageManager.getCurrentLanguageCode(), userID.toString())
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    when (response.code()) {
                        200 -> {
                            val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(UUIDAdapter())
                                .build()
                            val adapter: JsonAdapter<User> = moshi.adapter(User::class.java)
                            val user = adapter.fromJson(response.body().toString())
                            it.onSuccess(user!!)
                        }
                        else -> {
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    println("ONFAILURE")
                }
            })
        }
    }

    private fun onUsernameChanged(message: Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        Log.d("POTATO", "UserRepository::onUsernameChanged = $json")
        val usernameChanged = UserAdapter.jsonToUsernameChanged(jsonObject)
        if (!userCache.containsUser(usernameChanged.userID)) {
            getUser(usernameChanged.userID).subscribe { user ->
                user.username = usernameChanged.newUsername
                user.pictureID = usernameChanged.pictureID
                EventBus.getDefault().post(MessageEvent(EventType.USER_UPDATED, usernameChanged.userID))
            }
        } else {
            val user = userCache.getUser(usernameChanged.userID)
            user.username = usernameChanged.newUsername
            user.pictureID = usernameChanged.pictureID
            EventBus.getDefault().post(MessageEvent(EventType.USER_UPDATED, usernameChanged.userID))
        }
    }

    private fun handleSocketMessage(message: android.os.Message) {
        val socketMessage = message.obj as Message

        when (socketMessage.type) {
            com.log3900.socket.Event.USERNAME_CHANGED -> onUsernameChanged(socketMessage)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        socketService = SocketService.instance

        socketMessageHandler = Handler {
            handleSocketMessage(it)
            true
        }

        socketService?.subscribeToMessage(com.log3900.socket.Event.USERNAME_CHANGED, socketMessageHandler!!)
    }

    override fun onDestroy() {
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.USERNAME_CHANGED, socketMessageHandler!!)
        socketMessageHandler = null
        socketService = null
        instance = null
        super.onDestroy()
    }

    inner class UserRepositoryBinder : Binder() {
        fun getService(): UserRepository = this@UserRepository
    }
}