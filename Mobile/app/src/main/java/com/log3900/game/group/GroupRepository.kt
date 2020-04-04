package com.log3900.game.group

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.architecture.DialogEventMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.exceptions.BadNetworkResponseException
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.UUIDUtils
import com.log3900.utils.format.moshi.GroupAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

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

        refreshRepository()

        socketService?.subscribeToMessage(com.log3900.socket.Event.USER_JOINED_GROUP, socketMessageHandler!!)
        socketService?.subscribeToMessage(com.log3900.socket.Event.USER_LEFT_GROUP, socketMessageHandler!!)
        socketService?.subscribeToMessage(com.log3900.socket.Event.JOIN_GROUP_RESPONSE, socketMessageHandler!!)
        socketService?.subscribeToMessage(com.log3900.socket.Event.GROUP_CREATED, socketMessageHandler!!)
        socketService?.subscribeToMessage(com.log3900.socket.Event.GROUP_DELETED, socketMessageHandler!!)
        socketService?.subscribeToMessage(com.log3900.socket.Event.START_MATCH_RESPONSE, socketMessageHandler!!)
    }

    fun getGroups(sessionToken: String, forceReload: Boolean = false): Single<ArrayList<Group>> {
        val single = Single.create<ArrayList<Group>> {
            if (groupCache.needsReload || forceReload) {
                val call = GroupRestService.service.getGroups(sessionToken, LanguageManager.getCurrentLanguageCode())
                call.enqueue(object : Callback<JsonArray> {
                    override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                        when (response.code()) {
                            200 -> {
                                val groups = arrayListOf<Group>()

                                response.body()?.forEach { group ->
                                    groups.add(GroupAdapter().fromJson(group.asJsonObject))
                                }
                                groupCache.needsReload = false
                                groupCache.setGroups(groups)
                                it.onSuccess(groupCache.getAllGroups())
                            }
                            else -> {
                                val jsonObject = JsonParser().parse(response.errorBody()?.string()).asJsonObject
                                val errorMessage = jsonObject.get("Error").asString
                                it.onError(BadNetworkResponseException(errorMessage))
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                        it.onError(BadNetworkResponseException(t.message?: "getGroups failure"))
                    }
                })
            } else {
                it.onSuccess(groupCache.getAllGroups())
            }
        }

        return single.subscribeOn(Schedulers.io())
    }
    
    fun getGroup(sessionToken: String, groupID: UUID): Single<Group> {
        return Single.create {
            val call = GroupRestService.service.getGroup(sessionToken, LanguageManager.getCurrentLanguageCode(), groupID.toString())
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
            val call = GroupRestService.service.createGroup(sessionToken, LanguageManager.getCurrentLanguageCode(), group.toJsonObject())
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
                            val jsonObject = JsonParser().parse(response.errorBody()?.string()).asJsonObject
                            val errorMessage = jsonObject.get("Error").asString
                            it.onError(BadNetworkResponseException(errorMessage))
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    it.onError(BadNetworkResponseException(t.message?: "createGroup failure"))
                }
            })
        }
    }

    fun joinGroup(groupID: UUID) {
        socketService?.sendMessage(Event.JOIN_GROUP_REQUEST, UUIDUtils.uuidToByteArray(groupID))
    }

    fun leaveGroup(groupID: UUID) {
        socketService?.sendMessage(Event.LEAVE_GROUP_REQUEST, byteArrayOf())
    }

    fun kickPlayer(player: Player) {
        socketService?.sendMessage(Event.KICK_PLAYER, UUIDUtils.uuidToByteArray(player.ID))
    }

    fun addVirtualPlayers(amountOfVirtualPlayers: Byte) {
        val jsonRequest = JsonObject().apply {
            addProperty("NbJoueurs", amountOfVirtualPlayers)
        }
        socketService?.sendJsonMessage(Event.ADD_VIRTUAL_PLAYER, jsonRequest.toString())
    }

    fun refreshRepository() {
        groupCache = GroupCache()
        getGroups(AccountRepository.getInstance().getAccount().sessionToken).subscribe(
            {
                isReady = true
            },
            {

            }
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        socketMessageHandler = Handler {
            handleSocketMessage(it)
            true
        }
        initializeRepository()
    }

    private fun handleSocketMessage(message: Message) {
        val socketMessage = message.obj as com.log3900.socket.Message

        when (socketMessage.type) {
            com.log3900.socket.Event.USER_JOINED_GROUP -> onUserJoinedGroup(socketMessage)
            com.log3900.socket.Event.USER_LEFT_GROUP -> onUserLeftGroup(socketMessage)
            com.log3900.socket.Event.JOIN_GROUP_RESPONSE -> onJoinGroupResponse(socketMessage)
            com.log3900.socket.Event.GROUP_CREATED -> onGroupCreated(socketMessage)
            com.log3900.socket.Event.GROUP_DELETED-> onGroupDeleted(socketMessage)
            Event.START_MATCH_RESPONSE -> onMatchStartResponse(socketMessage)
        }
    }

    private fun onMatchStartResponse(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject
        val response = jsonObject.get("Response").asBoolean
        if (response) {
            EventBus.getDefault().post(MessageEvent(EventType.MATCH_START_RESPONSE, Pair(response, "")))
        } else {
            EventBus.getDefault().post(MessageEvent(EventType.SHOW_ERROR_MESSAGE, DialogEventMessage("Error", jsonObject.get("Error").asString, null, null)))
        }
    }

    private fun onUserJoinedGroup(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject

        val userID = UUID.fromString(jsonObject.get("UserID").asString)
        val groupID = UUID.fromString(jsonObject.get("GroupID").asString)
        val username = jsonObject.get("Username").asString
        val player = Player(
            UUID.fromString(jsonObject.get("UserID").asString),
            jsonObject.get("Username").asString,
            jsonObject.get("IsCPU").asBoolean
        )

        groupCache.addUserToGroup(groupID, player)

        if (player.ID != AccountRepository.getInstance().getAccount().ID) {
            EventBus.getDefault().post(MessageEvent(EventType.PLAYER_JOINED_GROUP, Pair(groupID, player.ID)))
        } else {
            EventBus.getDefault().post(MessageEvent(EventType.GROUP_JOINED, groupCache.getGroup(groupID)!!))
        }
    }

    private fun onUserLeftGroup(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject

        val userLeftGroup = GroupAdapter().jsonToUserLeftGroup(jsonObject)

        if (userLeftGroup.userID == AccountRepository.getInstance().getAccount().ID && groupCache.containsGroup(userLeftGroup.groupID)) {
            EventBus.getDefault().post(MessageEvent(EventType.GROUP_LEFT, userLeftGroup))
        }

        groupCache.removeUserFromGroup(userLeftGroup.groupID, userLeftGroup.userID)
        EventBus.getDefault().post(MessageEvent(EventType.PLAYER_LEFT_GROUP, userLeftGroup))
    }

    private fun onJoinGroupResponse(message: com.log3900.socket.Message) {
        val json = MoshiPack.msgpackToJson(message.data)
        val jsonObject = JsonParser().parse(json).asJsonObject

        val response = jsonObject.get("Response").asBoolean
        if (!response) {
            val error = jsonObject.get("Error").asString
            EventBus.getDefault().post(MessageEvent(EventType.SHOW_ERROR_MESSAGE, DialogEventMessage("Error", error, null, null)))
        }
    }

    private fun onGroupCreated(message: com.log3900.socket.Message) {

        val json = MoshiPack.msgpackToJson(message.data)
        val jsonGson = JsonParser().parse(json).asJsonObject

        val group = GroupAdapter().fromJson(jsonGson)
        groupCache.addGroup(group)

        EventBus.getDefault().post(MessageEvent(EventType.GROUP_CREATED, group))
    }

    private fun onGroupDeleted(message: com.log3900.socket.Message) {
        val groupID = UUIDUtils.byteArrayToUUID(message.data)
        groupCache.removeGroup(groupID)
        EventBus.getDefault().post(MessageEvent(EventType.GROUP_DELETED, groupID))
    }

    override fun onDestroy() {
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.START_MATCH_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.USER_JOINED_GROUP, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.USER_LEFT_GROUP, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.JOIN_GROUP_RESPONSE, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.GROUP_CREATED, socketMessageHandler!!)
        socketService?.unsubscribeFromMessage(com.log3900.socket.Event.GROUP_DELETED, socketMessageHandler!!)
        socketMessageHandler = null
        socketService = null
        instance = null
        super.onDestroy()
    }

    inner class GroupRepositoryBinder : Binder() {
        fun getService(): GroupRepository = this@GroupRepository
    }
}