package com.log3900.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
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
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.net.SocketTimeoutException

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
    }

    fun sendLoginInfo(view: View) {
        val username = findViewById<TextInputEditText>(R.id.username).text.toString()
        val validLoginInfo: Boolean = validateLoginInfo()
        if (!validLoginInfo)
            return
        changeLoadingView(true)

        // TODO: Send login info
        println("No errors")

        val authData = AuthenticationRequest(username)
        val call = RestClient.authenticationService.authenticate(authData)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>?, response: Response<ResponseBody?>?) {
                val message: String? = response?.body()?.string() ?: "Error with response body"
                println("(${response?.code()}) $message")
                changeLoadingView(false)

                // TODO: Change the code to get a JSON response instead of a raw one
                val jsonResponse = JSONObject(message)
                println(jsonResponse)
                if (jsonResponse.has("SessionToken")) {
                    println("found sessionToken")
                    SocketService.instance.subscribe(Event.SERVER_RESPONSE, Handler {
                        println("inside handler")
                        if ((it.obj as Message).data[0].toInt() == 1) {
                            startMainActivity(username)
                            true
                        }
                        else {
                            println("connection refused")
                            // TODO: Confirm if this returns
                            MaterialAlertDialogBuilder(this@LoginActivity)
                                .setMessage("Error: Connection refused.")
                                .setPositiveButton("OK", null)
                                .show()
                            changeLoadingView(false)
                            false
                        }
                    })

                    println("sending request to server")
                    SocketService.instance.sendMessage(Event.SOCKET_CONNECTION, (jsonResponse.get("SessionToken") as String).toByteArray(Charsets.UTF_8))
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                val errMessage: String =
                    if (t is SocketTimeoutException)
                        "Error: Timeout"
                    else
                        "Error: Couldn't authenticate ($t)"
                println(errMessage)

                MaterialAlertDialogBuilder(this@LoginActivity)
                    .setMessage("$errMessage. Please retry.")
                    .setPositiveButton("OK", null)
                    .show()
                changeLoadingView(false)
            }
        })
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
