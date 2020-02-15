package com.log3900.login.register

import com.google.gson.JsonObject
import com.log3900.login.AuthenticationRestService
import com.log3900.shared.ui.ProfilePresenter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class TokenData(val session: String, val bearer: String?)

class RegisterPresenter(registerFragment: RegisterFragment) : ProfilePresenter(registerFragment) {
    override val profileView = registerFragment

    fun register(username: String, password: String, pictureID: Int, email: String,
                 firstName: String, lastName: String) {
        sendRegisterRequest(username, password, pictureID, email, firstName, lastName)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ tokenData -> profileView.onRegisterSuccess() },
                { err -> profileView.onRegisterError(err.toString()) }
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
}