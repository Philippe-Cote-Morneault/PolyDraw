package com.log3900.profile

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.ui.ProfilePresenter
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.ServerErrorFormatter
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class NullableAccount(
    val username:   String?,
    val password:   String?,
    val pictureID:  Int?,
    val email:      String?,
    val firstName:  String?,
    val lastName:   String?
)

class ModifyProfilePresenter(modifyDialog: ModifyProfileDialog) : ProfilePresenter(modifyDialog) {
    override val profileView = modifyDialog

    fun updateAccountInfo(oldAccount: Account, updatedAccount: Account, password: String?) {
        val updatedInfo = NullableAccount(
            if (oldAccount.username != updatedAccount.username) updatedAccount.username else null,
            password,
            if (oldAccount.pictureID != updatedAccount.pictureID) updatedAccount.pictureID else null,
            if (oldAccount.email != updatedAccount.email) updatedAccount.email else null,
            if (oldAccount.firstname != updatedAccount.firstname) updatedAccount.firstname else null,
            if (oldAccount.lastname != updatedAccount.lastname) updatedAccount.lastname else null
        )
        sendUpdatedInfo(updatedInfo, oldAccount.sessionToken).observeOn(AndroidSchedulers.mainThread()).subscribe(
            { success ->
                applyModifications(updatedAccount)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        profileView.onModifySuccess(updatedAccount)
                }
            },
            { error -> profileView.onModifyError(error.message!!)}
        )
    }

    fun sendUpdatedInfo(updatedInfo: NullableAccount, sessionToken: String): Single<Boolean> {
        return Single.create { res ->
            val modifiedUserJson = JsonObject().apply {
                updatedInfo.username?.let { addProperty("Username", it) }
                updatedInfo.password?.let { addProperty("Password", it) }
                updatedInfo.pictureID?.let { addProperty("PictureID", it) }
                updatedInfo.email?.let { addProperty("Email", it) }
                updatedInfo.firstName?.let { addProperty("FirstName", it) }
                updatedInfo.lastName?.let { addProperty("LastName", it) }
            }
            val call = ProfileRestService.service.modifyProfile(
                sessionToken,
                LanguageManager.getCurrentLanguageCode(),
                modifiedUserJson
            )
            call.enqueue(object : Callback<JsonPrimitive> {
                override fun onResponse(call: Call<JsonPrimitive>, response: Response<JsonPrimitive>) {
                    if (response.isSuccessful) {
                        res.onSuccess(true)
                    } else {
                        val error = response.errorBody()?.string()
                        if (error != null) {
                            res.onError(Throwable(ServerErrorFormatter.format(error)))
                        } else {
                            res.onError(Throwable("Internal error"))
                        }
                    }
                }

                override fun onFailure(call: Call<JsonPrimitive>, t: Throwable) {
                    res.onError(t)
                }
            })
        }
    }

    private fun applyModifications(updatedAccount: Account): Completable {
        return AccountRepository.getInstance().updateAccount(updatedAccount)
    }
}