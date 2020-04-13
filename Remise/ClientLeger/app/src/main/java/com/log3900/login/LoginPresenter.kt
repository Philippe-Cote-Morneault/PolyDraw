package com.log3900.login

import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import com.google.gson.JsonObject
import com.log3900.MainActivity
import com.log3900.settings.language.LanguageManager
import com.log3900.shared.architecture.Presenter
import com.log3900.shared.ui.dialogs.ProgressDialog
import com.log3900.socket.*
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.ServerErrorFormatter
import io.reactivex.Completable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.*

class LoginPresenter(var loginView: LoginView?) : Presenter {
    private var rememberUser = false
    private val socketService = SocketService.instance!!
    private val socketHandler = Handler {
        loginWithBearer()
        true
    }

    init {
        // TODO: Unsubscribe?
        if (socketService.getSocketState() == State.CONNECTED) {
            loginWithBearer()
        } else {
            socketService.subscribeToEvent(SocketEvent.CONNECTED, socketHandler)
        }
    }

    private fun loginWithBearer() {
        val bearer = UserPrefsManager.getBearer()
        val username = UserPrefsManager.getUsername()
        Log.d("BEARER", "username: $username, bearer: $bearer")
        if (bearer == null || username == null) {
            return
        }
        val bearerLoginDialog = ProgressDialog()
        loginView?.showProgressDialog(bearerLoginDialog)
        loginView?.disableView()

        val json = JsonObject().apply {
            addProperty("Bearer", bearer)
            addProperty("username", username)
        }

        val call = AuthenticationRestService.service.bearerAuthenticate(
            "EN", // LanguageManager.getCurrentLanguage().languageCode,
            json
        )

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when(response.code()) {
                    200 -> {
                        val sessionToken = response.body()!!.get("SessionToken").asString
                        val bearerToken = response.body()!!.get("Bearer").asString
                        val userID = response.body()!!.get("UserID").asString
                        handleSuccessAuth(bearerToken, sessionToken, userID) {
                            loginView?.showWelcomeBackMessage(username)
                            loginView?.hideProgressDialog(bearerLoginDialog)
                        }
                    }
//                    401 -> handleErrorAuth("Your session has expired. Please log in again.")
                    else -> { // Fail silently...
                        Log.d("BEARER", "BEARER ERROR: ${response.errorBody()?.string()}")
                        UserPrefsManager.resetAll()
                        loginView?.hideProgressDialog(bearerLoginDialog)
                        loginView?.enableView()
//                        handleErrorAuth(response.errorBody()?.string() ?: "Internal error")
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                UserPrefsManager.resetAll()
                loginView?.hideProgressDialog(bearerLoginDialog)
                loginView?.enableView()
//                loginView?.showErrorDialog(
//                    "Error",
//                    "Error during authentication (Bearer token)",
//                    null,
//                    null
//                )
            }
        })
    }

    fun authenticate(username: String, password: String, language: String) {
        loginView?.showProgresBar()

        val authJson = JsonObject()
        authJson.addProperty("Username", username)
        authJson.addProperty("Password", password)

        val call = AuthenticationRestService.service.authenticate(language, authJson)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when(response.code()) {
                    200 -> {
                        val sessionToken = response.body()!!.get("SessionToken").asString
                        val bearerToken = response.body()!!.get("Bearer").asString
                        val userID = response.body()!!.get("UserID").asString
                        handleSuccessAuth(bearerToken, sessionToken, userID, language)
                    }
                        else -> {
                            val error = response.errorBody()?.string()
                            if (error != null) {
                                handleErrorAuth(ServerErrorFormatter.format(error))
                            } else {
                                handleErrorAuth(LoginErrorType.AUTH_ERROR)
                            }
                        }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                val errMessage: String =
                    if (t is SocketTimeoutException)
                        "The connection took too long"
                    else
                        "Couldn't authenticate ($t)"
                handleErrorAuth(LoginErrorType.CONNECTION_TIMEOUT)
            }
        })
    }

    private fun handleSuccessAuth(
        bearer: String,
        session: String,
        userID: String,
        language: String = "",
        onMainStart: () -> Unit = {}
    ) {
        Log.d("SESSION_TOKEN", session)
        SocketService.instance?.subscribeToMessage(Event.SERVER_RESPONSE, Handler {
            if ((it.obj as Message).data[0].toInt() == 1) {
                startMainActivity()
                onMainStart()
                true
            } else {
                handleErrorAuth(LoginErrorType.CONNECTION_REFUSED)
                false
            }
        })

        getUserInfo(session, bearer, userID, language).subscribe {
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

    private fun getUserInfo(sessionToken: String, bearerToken: String, userID: String, language: String): Completable {
        println("Getting user info...")
        return Completable.create {
            val call = AuthenticationRestService.service.getUserInfo(sessionToken, userID)
            call.enqueue(object: Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    when (response.code()) {
                        200 -> {
                            println("Sucessful user response")
                            val account = parseJsonAccount(response.body()!!)
                            storeUser(account, sessionToken, bearerToken, language)
                                .subscribe {
                                    it.onComplete()
                                }
                        }

                        else -> {
                            handleErrorAuth(LoginErrorType.GET_ACCOUNT_INFO)
                            it.onComplete()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    handleErrorAuth(LoginErrorType.GET_ACCOUNT_INFO)
                }
            })
        }
    }

    private fun storeUser(account: Account, sessionToken: String, bearerToken: String, language: String): Completable {
        return Completable.create {completable ->
            AccountRepository.getInstance().getAccountByID(account.ID).subscribe(
                {
                    AccountRepository.getInstance().createAccount(
                        account.copy(
                            sessionToken = sessionToken,
                            bearerToken = bearerToken,
                            tutorialDone = it.tutorialDone,
                            soundEffectsOn = it.soundEffectsOn,
                            themeID = it.themeID,
                            languageID = if (language == "") {
                                it.languageID
                            } else {
                                LanguageManager.getLanguageIDByCode(language) // it.languageID,
                            },
                            musicOn = it.musicOn
                        )
                    ).subscribe {
                        AccountRepository.getInstance().setCurrentAccount(account.ID)
                            .subscribe{
                                if (rememberUser) {
                                    UserPrefsManager.storeBearer(bearerToken)
                                    UserPrefsManager.storeUsername(account.username)
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
                                    UserPrefsManager.storeBearer(bearerToken)
                                    UserPrefsManager.storeUsername(account.username)
                                }
                                completable.onComplete()
                            }
                    }
                })
        }
    }

    private fun handleErrorAuth(error: String) {
        loginView?.showErrorDialog("Authentication error", error, null, null, null)
        loginView?.hideProgressBar()
    }

    private fun handleErrorAuth(errorType: LoginErrorType) {
        loginView?.showErrorDialog("Authentication error", "", errorType, null, null)
        loginView?.hideProgressBar()
    }

    fun startMainActivity() {
        loginView?.navigateTo(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun validateUsername(username: String): Boolean {
        if (!Validator.validateUsername(username)) {
            loginView?.setUsernameError("Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)")
            return false
        } else {
            loginView?.clearUsernameError()
            return true
        }
    }

    fun validatePassword(password: String): Boolean {
        if (!Validator.validatePassword(password)) {
            loginView?.setPasswordError("Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)")
            return false
        } else {
            loginView?.clearPasswordError()
            return true
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
                        LoginErrorType.SOCKET_CONNECTION_TIMEOUT,
                        {_, _ ->
                            loginView?.closeView()
                        },
                        null
                    )
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