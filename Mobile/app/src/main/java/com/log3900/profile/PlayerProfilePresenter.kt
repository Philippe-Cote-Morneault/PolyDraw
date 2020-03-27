package com.log3900.profile

import com.google.gson.JsonObject
import com.log3900.login.AuthenticationRestService
import com.log3900.user.User
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

private data class PublicUserInfo(val username: String, val avatarID: Int)

class PlayerProfilePresenter(
    val profileDialog: PlayerProfileDialogFragment,
    val userID: UUID
) {
    fun fetchUserInfo() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                getUserInfo()
            }
        }
    }

    private fun getUserInfo() {
        val call = AuthenticationRestService.service.getUserInfo(
            AccountRepository.getInstance().getAccount().sessionToken,
            userID.toString()
        )

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when (response.code()) {
                    200 -> {
                        val userInfo = response.body()!!.toPublicUser()
                        fillUserInfo(userInfo)
                    }
                    else -> {
                        onError("Error: Could not get this user's information")
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError("Error while trying to get this user's information")
            }
        })
    }

    private fun fillUserInfo(userInfo: PublicUserInfo) {
        profileDialog.setUsername(userInfo.username)
        profileDialog.setAvatar(userInfo.avatarID)
    }

    private fun onError(error: String) {
        profileDialog.dismiss()
    }

    private fun JsonObject.toPublicUser() = PublicUserInfo(
        get("Username").asString,
        get("PictureID").asInt
    )
}