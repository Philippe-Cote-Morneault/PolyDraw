package com.log3900.login

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.EditText
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
            val usernameLayout: TextInputLayout = findViewById<TextInputLayout>(R.id.login_username_layout)
            usernameLayout.error = "Invalid name (must be 4-12 alphanumeric characters)"
            return
        } else if (!Validator.validatePassword(password)) {
            val passwordLayout: TextInputLayout = findViewById<TextInputLayout>(R.id.login_password_layout)
            passwordLayout.error = "Invalid password (must be 4-12 characters)"
            return
        }

        // TODO: Send login info
        println("No errors")
    }
}
