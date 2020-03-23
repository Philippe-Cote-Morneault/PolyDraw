package com.log3900.login

import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import com.google.gson.JsonObject
import com.log3900.MainActivity
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.architecture.Presenter
import com.log3900.shared.ui.dialogs.ProgressDialog
import com.log3900.socket.*
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import io.reactivex.Completable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.*

class LoginPresenter(var loginView: LoginView?) : Presenter {
    private var rememberUser = false

    fun authenticate(username: String, password: String) {
        loginView?.showProgresBar()

        val authJson = JsonObject()
        authJson.addProperty("Username", username)
        authJson.addProperty("Password", password)

        val call = AuthenticationRestService.service.authenticate(authJson)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when(response.code()) {
                    200 -> {
                        val sessionToken = response.body()!!.get("SessionToken").asString
                        val bearerToken = response.body()!!.get("Bearer").asString
                        val userID = response.body()!!.get("UserID").asString
                        handleSuccessAuth(bearerToken, sessionToken, username, userID)
                    }
                        else -> {
                        handleErrorAuth(response.errorBody()?.string() ?: "Internal error")
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                val errMessage: String =
                    if (t is SocketTimeoutException)
                        "The connection took too long"
                    else
                        "Couldn't authenticate ($t)"
                handleErrorAuth(errMessage)
            }
        })
    }

    private fun handleSuccessAuth(bearer: String, session: String, username: String, userID: String) {
        SocketService.instance?.subscribeToMessage(Event.SERVER_RESPONSE, Handler {
            if ((it.obj as Message).data[0].toInt() == 1) {
                startMainActivity()
                true
            } else {
                handleErrorAuth("Connection refused.")
                false
            }
        })

        getUserInfo(session, bearer, userID).subscribe {
            SocketService.instance?.sendMessage(
                Event.SOCKET_CONNECTION,
                session.toByteArray(Charsets.UTF_8))
        }
//        val account = getUserInfo(session, bearer, ID)
//        if (account == null) {
//            handleErrorAuth("Error while trying to get account information")
//            return
//        }
//        storeUser(account, session, bearer)
    }

    private fun getUserInfo(sessionToken: String, bearerToken: String, userID: String): Completable {
        println("Getting user info...")
        return Completable.create {
            val call = AuthenticationRestService.service.getUserInfo(sessionToken, userID)
            call.enqueue(object: Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    when (response.code()) {
                        200 -> {
                            println("Sucessful user response")
                            val account = parseJsonAccount(response.body()!!)
                            storeUser(account, sessionToken, bearerToken)
                                .subscribe {
                                    it.onComplete()
                                }
                        }

                        else -> {
                            handleErrorAuth("Error while getting account information.")
                            it.onComplete()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    handleErrorAuth(t.toString())
                }
            })
        }
    }

    private fun storeUser(account: Account, sessionToken: String, bearerToken: String): Completable {
        return Completable.create {completable ->
            AccountRepository.getInstance().getAccountByID(account.ID).subscribe(
                {
                    AccountRepository.getInstance().createAccount(
                        account.copy(
                            sessionToken = sessionToken,
                            bearerToken = bearerToken,
                            tutorialDone = it.tutorialDone,
                            themeID = it.themeID,
                            languageID =  it.languageID,
                            soundEffectsOn = it.soundEffectsOn,
                            musicOn = it.musicOn
                        )
                    ).subscribe {
                        AccountRepository.getInstance().setCurrentAccount(account.ID)
                            .subscribe{
                                if (rememberUser) {
                                    BearerTokenManager.storeBearer(bearerToken)
                                }
                                completable.onComplete()
                            }
                    }
                },
                {
                    AccountRepository.getInstance().createAccount(
                        account.copy(
                            sessionToken = sessionToken,
                            bearerToken = bearerToken
                        )
                    ).subscribe {
                        AccountRepository.getInstance().setCurrentAccount(account.ID)
                            .subscribe{
                                if (rememberUser) {
                                    BearerTokenManager.storeBearer(bearerToken)
                                }
                                completable.onComplete()
                            }
                    }
                })
        }
    }

    private fun handleErrorAuth(error: String) {
        loginView?.showErrorDialog("Authentication error", error, null, null)
        loginView?.hideProgressBar()
    }

    fun startMainActivity() {
        loginView?.navigateTo(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun validateUsername(username: String) {
        if (!Validator.validateUsername(username)) {
            loginView?.setUsernameError("Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)")
        } else {
            loginView?.clearUsernameError()
        }
    }

    fun validatePassword(password: String) {
        if (!Validator.validatePassword(password)) {
            loginView?.setPasswordError("Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)")
        } else {
            loginView?.clearPasswordError()
        }
    }

    fun rememberUser() {
        rememberUser = true
    }

    override fun destroy() {
        loginView = null
    }

    override fun resume() {
        if (SocketService.instance?.getSocketState() != State.CONNECTED) {
            val socketConnectionDialog = ProgressDialog()
            loginView?.showProgressDialog(socketConnectionDialog)
            val timer = object: CountDownTimer(60000, 15000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (SocketService.instance?.getSocketState() != State.CONNECTED) {
                        SocketService.instance?.connectToSocket()
                    }
                }

                override fun onFinish() {
                    loginView?.hideProgressDialog(socketConnectionDialog)
                    loginView?.showErrorDialog("Connection Error",
                        "Could not establish connection to server after 4 attempts. The application will now close.",
                        null,
                        {_, _ ->
                            loginView?.closeView()
                        })
                }
            }
            SocketService.instance?.subscribeToEvent(SocketEvent.CONNECTED, Handler{
                socketConnectionDialog.dismiss()
                timer.cancel()
                true
            })
            timer.start()
        }
    }

    override fun pause() {

    }

    private fun parseJsonAccount(json: JsonObject): Account {
        return Account(
            UUID.fromString(json.get("ID").asString),
            json.get("Username").asString,
            json.get("PictureID").asInt,
            json.get("Email").asString,
            json.get("FirstName").asString,
            json.get("LastName").asString,
            "",     // Session token and bearer token are not important right now
            "",
            0,
            LanguageManager.LANGUAGE.SYSTEM.ordinal,
            false,
            true,
            true
        )
    }
}