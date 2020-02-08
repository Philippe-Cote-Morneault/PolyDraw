package com.log3900.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Callback
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.log3900.MainActivity
import com.log3900.R
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.shared.ui.ProgressDialog
import com.log3900.socket.*
import com.log3900.utils.ui.KeyboardHelper
import retrofit2.Call
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.reflect.jvm.internal.ReflectProperties

class LoginActivity : AppCompatActivity() {
    // Services
    private lateinit var loginService: LoginService
    // UI Elements
    private lateinit var loginButton: MaterialButton
    private lateinit var usernameTextInput: TextInputEditText
    private lateinit var usernameTextInputLayout: TextInputLayout
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var passwordTextInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginService = LoginService()

        setupUIElements()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun setupUIElements() {
        loginButton = findViewById(R.id.activity_login_login_button)
        loginButton.setOnClickListener {
            onLoginButtonClick()
        }

        usernameTextInput = findViewById(R.id.activity_login_text_input_username)
        usernameTextInput.doAfterTextChanged {
            onUsernameChange()
        }
        passwordTextInput = findViewById(R.id.activity_login_text_input_password)
        passwordTextInput.doAfterTextChanged {
            onPasswordChange()
        }

        usernameTextInputLayout = findViewById(R.id.activity_login_text_input_layout_username)
        passwordTextInputLayout = findViewById(R.id.activity_login_text_input_layout_password)

        supportActionBar?.hide()
    }

    private fun onUsernameChange() {
        if (!Validator.validateUsername(usernameTextInput.text.toString())) {
            usernameTextInputLayout.error =
                "Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)"
        } else {
            usernameTextInputLayout.error = null
        }
    }

    private fun onPasswordChange() {
        if (!Validator.validatePassword(passwordTextInput.text.toString())) {
            passwordTextInputLayout.error =
                "Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)"
        } else {
            passwordTextInputLayout.error = null
        }
    }

    override fun onResume() {
        super.onResume()
        if (SocketService.instance?.getSocketState() != State.CONNECTED) {
            val socketConnectionDialog = ProgressDialog()
            socketConnectionDialog.show(supportFragmentManager, "progressDialog")
            val timer = object: CountDownTimer(60000, 15000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (SocketService.instance?.getSocketState() != State.CONNECTED) {
                        SocketService.instance?.connectToSocket()
                    }
                }

                override fun onFinish() {
                    socketConnectionDialog.dismiss()
                    AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Connection Error")
                        .setMessage("Could not establish connection to server after 4 attempts. The application will now close.")
                        .setPositiveButton("Ok") { dialog, which ->
                            finishAffinity()
                        }
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
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

    private fun onLoginButtonClick() {
        val username = usernameTextInput.text.toString().toLowerCase()
        KeyboardHelper.hideKeyboard(this)

        if (!Validator.validateUsername(usernameTextInput.text.toString()) || !Validator.validatePassword(passwordTextInput.text.toString())) {
            // TODO: Add toast or popup letting user know he needs to fix credentials
            return
        }
        changeLoadingView(true)

        val authJson = JsonObject()
        authJson.addProperty("Username", username)
        val call = AuthenticationRestService.service.authenticate(authJson)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when(response.code()) {
                    200 -> {
                        val sessionToken = response.body()!!.get("SessionToken").asString
                        val bearerToken = response.body()!!.get("Bearer").asString
                        handleSuccessAuth(bearerToken, sessionToken)
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
                println(errMessage)
                handleErrorAuth(errMessage)
            }
        })
    }

    private fun handleSuccessAuth(bearer: String, session: String) {
        SocketService.instance?.subscribeToMessage(Event.SERVER_RESPONSE, Handler {
            println("inside handler")
            if ((it.obj as Message).data[0].toInt() == 1) {
                val username = findViewById<TextInputEditText>(R.id.activity_login_text_input_username).text.toString().toLowerCase()
                startMainActivity(username)
                true
            } else {
                handleErrorAuth("Connection refused.")
                false
            }
        })

        SocketService.instance?.sendMessage(
            Event.SOCKET_CONNECTION,
            session.toByteArray(Charsets.UTF_8))
    }


    private fun handleErrorAuth(error: String) {
        MaterialAlertDialogBuilder(this@LoginActivity)
            .setMessage("Error: $error")
            .setPositiveButton("Retry", null) //{ _, _ -> onLoginButtonClick() }
            .setNegativeButton("Cancel", null)
            .show()
        changeLoadingView(false)
    }

    fun startMainActivity(username: String) {
        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        if (preferences != null) {
            with (preferences.edit()) {
                putString(getString(R.string.preference_file_username_key), username)
                commit()
            }
        }
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun changeLoadingView(isLoading: Boolean) {
        val progressBar: ProgressBar = findViewById(R.id.activity_login_progressbar_login)

        if (isLoading) {
            loginButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}
