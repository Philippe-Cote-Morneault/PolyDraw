package com.log3900.user

import android.util.Log
import com.google.gson.JsonObject
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import retrofit2.Call
import java.util.*
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.HashMap

class UserRepository {
    private var userCache: UserCache = UserCache()
    private var ongoingRequests: HashMap<UUID, Single<User>> = HashMap()

    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            if (instance == null) {
                instance = UserRepository()
            }

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
                        userCache.addUser(user)
                        ongoingRequests.remove(userID)
                        it.onSuccess(user)
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
            val call = UserRestService.service.getUser(AccountRepository.getInstance().getAccount().sessionToken, "EN", userID.toString())
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
}