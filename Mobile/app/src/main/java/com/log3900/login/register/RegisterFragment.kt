package com.log3900.login.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.login.LoginFragment
import com.log3900.shared.ui.ProfileView
import com.log3900.user.Account

class RegisterFragment : Fragment(), ProfileView {
    val registerPresenter = RegisterPresenter(this)

    lateinit var usernameInput: TextInputEditText
    lateinit var passwordInput: TextInputEditText
    lateinit var emailInput:    TextInputEditText
    lateinit var firstnameInput: TextInputEditText
    lateinit var lastnameInput: TextInputEditText

    lateinit var backBtn:   MaterialButton
    lateinit var registerBtn:   MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_register, container, false)
        setUpUi(root)
        return root
    }


    fun setUpUi(root: View) {
        registerBtn = root.findViewById(R.id.register_button)
        registerBtn.setOnClickListener {
            register()
        }

        backBtn = root.findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }

        usernameInput = root.findViewById<TextInputEditText>(R.id.username_input).apply {
            doAfterTextChanged {
                registerPresenter.validateUsername(text.toString())
                enableRegisterIfAllValid()
            }
        }

        passwordInput = root.findViewById<TextInputEditText>(R.id.password_input).apply {
            doAfterTextChanged {
                registerPresenter.validatePassword(text.toString())
                enableRegisterIfAllValid()
            }
        }

        emailInput = root.findViewById<TextInputEditText>(R.id.email_input).apply {
            doAfterTextChanged {
                registerPresenter.validateEmail(text.toString())
                enableRegisterIfAllValid()
            }
        }

        firstnameInput = root.findViewById<TextInputEditText>(R.id.firstname_input).apply {
            doAfterTextChanged {
                registerPresenter.validateFirstname(text.toString())
                enableRegisterIfAllValid()
            }
        }

        lastnameInput = root.findViewById<TextInputEditText>(R.id.lastname_input).apply {
            doAfterTextChanged {
                registerPresenter.validateLastname(text.toString())
                enableRegisterIfAllValid()
            }
        }
    }

    private fun register() {
        // TODO: Some kind of loading feedback
        registerPresenter.register(
            usernameInput.text.toString(),
            passwordInput.text.toString(),
            1,  // TODO: Change
            emailInput.text.toString(),
            firstnameInput.text.toString(),
            lastnameInput.text.toString()
        )
    }

    fun onRegisterSuccess() {
        val username = usernameInput.text.toString()
        MaterialAlertDialogBuilder(context)
            .setTitle("Registration completed")
            .setMessage("Account successfully created! Welcome, $username!")
            .setPositiveButton("OK", null)  // TODO: Actually log in
            .setCancelable(false)
            .show()
    }

    fun onRegisterError(error: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Error")
            .setMessage("An error occurred: $error")
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /**
     * Changes if the "Register" button is enabled
     */
    private fun enableRegisterIfAllValid() {
        val username    = usernameInput.text.toString()
        val password    = passwordInput.text.toString()
        val email       = emailInput.text.toString()
        val firstname   = firstnameInput.text.toString()
        val lastname    = lastnameInput.text.toString()

        // All fields are valid
        registerBtn.isEnabled = registerPresenter.validateUsername(username)
                && registerPresenter.validatePassword(password)
                && registerPresenter.validateEmail(email)
                && registerPresenter.validateFirstname(firstname)
                && registerPresenter.validateLastname(lastname)
    }

    override fun setUsernameError(error: String?) {
        usernameInput.error = error
    }
    override fun setPasswordError(error: String?) {
        passwordInput.error = error
    }
    override fun setEmailError(error: String?) {
        emailInput.error = error
    }
    override fun setFirstnameError(error: String?) {
        firstnameInput.error = error
    }
    override fun setLastnameError(error: String?) {
        lastnameInput.error = error
    }
}