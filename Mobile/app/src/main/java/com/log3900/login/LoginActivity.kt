package com.log3900.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Callback
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.log3900.MainActivity
import com.log3900.R
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.shared.ui.ProgressDialog
import com.log3900.socket.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.*
import kotlin.concurrent.timerTask

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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

    fun sendLoginInfo(view: View) {
        val username = findViewById<TextInputEditText>(R.id.username).text.toString().toLowerCase()
        hideKeyboard(view)
        val validLoginInfo: Boolean = validateLoginInfo()
        if (!validLoginInfo)
            return
        changeLoadingView(true)

        val authData = AuthenticationRequest(username)
        val call = RestClient.authenticationService.authenticate(authData)
        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val res: AuthResponse = response.body() ?: parseError(response.errorBody().string())
                when(response.code()) {
                    200 -> handleSuccessAuth(res.bearer!!, res.sessionToken!!)
                    else -> handleErrorAuth(res.error ?: "Internal error")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
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
        // TODO: Change the code to get a JSON response instead of a raw one
        println("found sessionToken")
        SocketService.instance?.subscribeToMessage(Event.SERVER_RESPONSE, Handler {
            println("inside handler")
            if ((it.obj as Message).data[0].toInt() == 1) {
                val username = findViewById<TextInputEditText>(R.id.username).text.toString().toLowerCase()
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
            .setPositiveButton("Retry", null) //{ _, _ -> sendLoginInfo() }
            .setNegativeButton("Cancel", null)
            .show()
        changeLoadingView(false)
    }

    private fun parseError(errorBody: String): AuthResponse {
        val json = JSONObject(errorBody)
        return AuthResponse(null, null, error = json["Error"].toString())
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

    // TODO: Utility function?
    private fun hideKeyboard(view: View) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun validateLoginInfo(): Boolean {
        val username = findViewById<TextInputEditText>(R.id.username).text.toString()
        val password = findViewById<TextInputEditText>(R.id.password).text.toString()
        val usernameLayout: TextInputLayout = findViewById(R.id.login_username_layout)
        val passwordLayout: TextInputLayout = findViewById(R.id.login_password_layout)

        if (!Validator.validateUsername(username)) {
            usernameLayout.error = "Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)"
            passwordLayout.error = null
            return false
        } else if (!Validator.validatePassword(password)) {
            usernameLayout.error = null
            passwordLayout.error = "Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)"
            return false
        }

        usernameLayout.error = null
        passwordLayout.error = null

        return true
    }

    private fun changeLoadingView(isLoading: Boolean) {
        val loginButton: Button = findViewById(R.id.login_btn)
        val progressBar: ProgressBar = findViewById(R.id.login_progressbar)

        if (isLoading) {
            loginButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}
