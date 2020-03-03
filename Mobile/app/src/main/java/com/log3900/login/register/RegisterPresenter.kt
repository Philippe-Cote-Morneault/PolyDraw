package com.log3900.login.register

import android.os.Handler
import com.google.gson.JsonObject
import com.log3900.login.AuthenticationRestService
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.ui.ProfilePresenter
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

data class TokenData(val session: String, val bearer: String?)

class RegisterPresenter(registerFragment: RegisterFragment) : ProfilePresenter(registerFragment) {
    override val profileView = registerFragment

    fun register(username: String, password: String, pictureID: Int, email: String,
                 firstName: String, lastName: String) {
        sendRegisterRequest(username, password, pictureID, email, firstName, lastName)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ tokenData ->
                onRegisterSuccess(username, password, pictureID, email,
                    firstName, lastName, tokenData)
            },
                { err ->
                    profileView.onRegisterError(err.toString())
                }
            )
    }

    private fun sendRegisterRequest(username: String, password: String, pictureID: Int,
                                    email: String, firstName: String, lastName: String)
    : Single<TokenData> {
        return Single.create {
            val accountInfo = JsonObject().apply {
                addProperty("Username", username)
                addProperty("Password", password)
                addProperty("PictureID", pictureID)
                addProperty("Email", email)
                addProperty("FirstName", firstName)
                addProperty("LastName", lastName)
            }

            // TODO: Language
            val call = AuthenticationRestService.service.register("EN", accountInfo)
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful && response.body() != null) {
                        it.onSuccess(parseResponseJson(response.body()!!))
                    } else {
                        it.onError(Throwable("(${response.code()}) ${response.errorBody()?.string()}"))
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }

    private fun onRegisterSuccess(username: String, password: String, pictureID: Int, email: String,
                                  firstName: String, lastName: String, tokenData: TokenData) {
        SocketService.instance?.subscribeToMessage(Event.SERVER_RESPONSE, Handler {
            if ((it.obj as Message).data[0].toInt() == 1) {
                profileView.onRegisterSuccess()
                true
            } else {
                profileView.onRegisterError("Connection refused")
                false
            }
        })

        AccountRepository.getInstance().createAccount(
            Account(
                UUID.randomUUID(),
                username.toLowerCase(),
                pictureID,
                email,
                firstName,
                lastName,
                tokenData.session,
                tokenData.bearer ?: "", // TODO: Actually handle the missing bearer token
                0,
                LanguageManager.LANGUAGE.SYSTEM.ordinal,
                false
            )
        ).subscribe {
            SocketService.instance?.sendMessage(
                Event.SOCKET_CONNECTION,
                tokenData.session.toByteArray(Charsets.UTF_8))
        }
    }



    private fun parseResponseJson(json: JsonObject): TokenData {
        val bearer =
            if (json.has("BearerToken"))
                json.get("BearerToken").asString
            else
                null
        return TokenData(
            json.get("SessionToken").asString,
            bearer
        )
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return if (password != confirmPassword) {
            profileView.setConfirmPasswordError("Password doesn't match")
            true
        } else {
            profileView.setConfirmPasswordError(null)
            true
        }
    }
}