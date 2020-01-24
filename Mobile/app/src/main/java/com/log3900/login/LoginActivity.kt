package com.log3900.login

import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.log3900.R

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
        Handler().postDelayed({
            println("hello from delayed")
            changeLoadingView(false)
        }, 2000L)
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
