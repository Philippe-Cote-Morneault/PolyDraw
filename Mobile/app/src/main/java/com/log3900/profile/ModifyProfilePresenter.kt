package com.log3900.profile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.ui.ProfilePresenter
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModifyProfilePresenter(modifyDialog: ModifyProfileDialog) : ProfilePresenter(modifyDialog) {
    override val profileView = modifyDialog

    fun updateAccountInfo(updatedAccount: Account, password: String?) {
        sendUpdatedInfo(updatedAccount, password).observeOn(AndroidSchedulers.mainThread()).subscribe(
            { success ->
                profileView.onModifySuccess(updatedAccount)
                AccountRepository.getInstance().updateAccount(updatedAccount)
            },
            { error -> profileView.onModifyError(error.toString()) }
        )
    }

    fun sendUpdatedInfo(updatedAccount: Account, password: String?): Single<Boolean> {
        return Single.create {
            val modifiedUserJson = JsonObject().apply {
                addProperty("Username", updatedAccount.username)
                addProperty("Password", password)
                addProperty("PictureID", updatedAccount.pictureID)
                addProperty("Email", updatedAccount.email)
                addProperty("FirstName", updatedAccount.firstname)
                addProperty("LastName", updatedAccount.lastname)
            }
            val call = ProfileRestService.service.modifyProfile(
                updatedAccount.sessionToken,
                LanguageManager.getCurrentLanguageCode(),
                modifiedUserJson
            )
            call.enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    println("${response.body()}")
                    if (response.isSuccessful) {
                        it.onSuccess(true)
                    } else {
                        it.onError(Throwable("(${response.code()}) ${response.errorBody()?.string()}"))
                    }
                }

                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }
}