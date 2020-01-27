package com.log3900.login

import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Callback
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.log3900.R
import okhttp3.ResponseBody
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
        val password = findViewById<TextInputEditText>(R.id.password).text.toString()

        if (!Validator.validateUsername(username)) {
            val usernameLayout: TextInputLayout = findViewById(R.id.login_username_layout)
            usernameLayout.error = "Invalid name (must be 4-12 alphanumeric characters)"
            return
        } else if (!Validator.validatePassword(password)) {
            val passwordLayout: TextInputLayout = findViewById(R.id.login_password_layout)
            passwordLayout.error = "Invalid password (must be 4-12 characters)"
            return
        }

        changeLoadingView(true)

        // TODO: Send login info
        println("No errors")

        val call = RestClient.testRequestService.getHello()
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>?, response: Response<ResponseBody?>?) {
                val message: String? = response?.body()?.string() ?: "Error with response body"
                println(message)
                MaterialAlertDialogBuilder(this@LoginActivity, R.style.Theme_MaterialComponents_Dialog_Alert)
                    .setMessage(message)
                    .setPositiveButton("Ok", null)
                    .show()

                changeLoadingView(false)
            }
            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                val errMessage: String =
                    if (t is SocketTimeoutException)
                        "Error: Timeout"
                    else
                        "Error: Couldn't authentificate"
                println(errMessage)
                MaterialAlertDialogBuilder(this@LoginActivity, R.style.Theme_MaterialComponents_Dialog_Alert)
                    .setMessage(errMessage)
                    .setPositiveButton("Ok", null)
                    .setNegativeButton("Retry", null)
                    .show()
                changeLoadingView(false)
            }
        })
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
