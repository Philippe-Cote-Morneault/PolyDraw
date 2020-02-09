package com.log3900.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.log3900.R
import com.log3900.socket.SocketService
import com.log3900.shared.ui.ProgressDialog
import com.log3900.socket.*
import com.log3900.utils.ui.KeyboardHelper

class LoginActivity : AppCompatActivity(), LoginView {
    // Services
    private lateinit var loginPresenter: LoginPresenter
    // UI Elements
    private lateinit var loginButton: MaterialButton
    private lateinit var usernameTextInput: TextInputEditText
    private lateinit var usernameTextInputLayout: TextInputLayout
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var passwordTextInputLayout: TextInputLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginPresenter = LoginPresenter(this)

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

        progressBar = findViewById(R.id.activity_login_progressbar_login)

        supportActionBar?.hide()
    }

    private fun onUsernameChange() {
        loginPresenter.validateUsername(usernameTextInput.text.toString())
    }

    private fun onPasswordChange() {
        loginPresenter.validatePassword(passwordTextInput.text.toString())
    }

    override fun onResume() {
        super.onResume()
        loginPresenter.resume()
    }

    private fun onLoginButtonClick() {
        KeyboardHelper.hideKeyboard(this)

        loginPresenter.authenticate(usernameTextInput.text.toString(), passwordTextInput.text.toString())
    }

    override fun showProgresBar() {
        progressBar.visibility = View.VISIBLE
        loginButton.visibility = View.INVISIBLE
    }

    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
        loginButton.visibility = View.VISIBLE
    }

    override fun setUsernameError(error: String) {
        usernameTextInputLayout.error = error
    }

    override fun setPasswordError(error: String) {
        passwordTextInputLayout.error = error
    }

    override fun clearPasswordError() {
        passwordTextInputLayout.error = null
    }

    override fun clearUsernameError() {
        usernameTextInputLayout.error = null
    }

    override fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage("Error: $error")
            .setPositiveButton("Retry", null) //{ _, _ -> onLoginButtonClick() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        loginPresenter.destroy()
        super.onDestroy()
    }

    override fun navigateTo(target: Class<*>, intentFlags: Int?) {
        val intent = Intent(this, target)
        if (intentFlags != null) {
            intent.flags = intentFlags
        }
        startActivity(intent)
        finish()
    }
}
