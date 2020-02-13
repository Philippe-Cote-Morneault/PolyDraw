package com.log3900.profile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.login.Validator
import com.log3900.user.Account
import com.log3900.user.AccountRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModifyProfilePresenter(var modifyDialog: ModifyProfileDialog) {

    fun updateAccountInfo(updatedAccount: Account, password: String?) {
        sendUpdatedInfo(updatedAccount, password).observeOn(AndroidSchedulers.mainThread()).subscribe(
            { success ->
                modifyDialog.onModifySuccess(updatedAccount)
                AccountRepository.updateAccount(updatedAccount)
            },
            { error -> modifyDialog.onModifyError(error.toString()) }
        )
    }

    fun sendUpdatedInfo(updatedAccount: Account, password: String?): Single<Boolean> {
        return Single.create {
            val modifiedUserJson = JsonObject().apply {
                addProperty("Username", updatedAccount.username)
                addProperty("Password", password)
                addProperty("Email", updatedAccount.email)
                addProperty("FirstName", updatedAccount.firstname)
                addProperty("LastName", updatedAccount.lastname)
            }
            val call = ProfileRestService.service.modifyProfile(updatedAccount.sessionToken, "EN", modifiedUserJson)
            call.enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
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

    fun validateUsername(username: String): Boolean {
        return if (!Validator.validateUsername(username)) {
            modifyDialog.setUsernameError("Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)")
            false
        } else {
            modifyDialog.setUsernameError(null)
            true
        }
    }

    fun validatePassword(password: String): Boolean {
        return if (!Validator.validatePassword(password)) {
            modifyDialog.setPasswordError("Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)")
            false
        } else {
            modifyDialog.setPasswordError(null)
            true
        }
    }

    fun validateEmail(email: String): Boolean {
        return if (!Validator.validateEmail(email)) {
            modifyDialog.setEmailError("Invalid email format")
            false
        } else {
            modifyDialog.setEmailError(null)
            true
        }
    }

    fun validateFirstname(firstname: String): Boolean {
        return if (firstname.isEmpty()) {
            modifyDialog.setFirstnameError("First name cannot be empty")
            false
        } else {
            modifyDialog.setFirstnameError(null)
            true
        }
    }
    fun validateLastname(lastname: String): Boolean {
        return if (lastname.isEmpty()) {
            modifyDialog.setLastnameError("Last name cannot be empty")
            false
        } else {
            modifyDialog.setLastnameError(null)
            true
        }
    }
}