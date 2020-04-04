package com.log3900.login.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.log3900.MainActivity
import com.log3900.R
import com.log3900.login.LoginActivity
import com.log3900.profile.ModifyAvatarDialog
import com.log3900.profile.ModifyAvatarDialogLauncher
import com.log3900.settings.LocaleLanguageHelper
import com.log3900.shared.architecture.ViewNavigator
import com.log3900.shared.ui.ProfileView
import com.log3900.utils.ui.getAvatarID
import kotlinx.android.synthetic.main.fragment_register.*
import kotlin.random.Random

class RegisterFragment : Fragment(), ProfileView, ModifyAvatarDialogLauncher, ViewNavigator {
    val registerPresenter = RegisterPresenter(this)

    var avatarIndex = Random.nextInt(1, 17) // From 1 to 16
    lateinit var avatarView: ImageView
    lateinit var usernameInput: TextInputEditText
    lateinit var passwordInput: TextInputEditText
    lateinit var confirmPasswordInput: TextInputEditText
    lateinit var emailInput: TextInputEditText
    lateinit var firstnameInput: TextInputEditText
    lateinit var lastnameInput: TextInputEditText

    lateinit var backBtn: MaterialButton
    lateinit var registerBtn: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_register, container, false)
        setUpUi(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        changeResLanguage((activity as LoginActivity).currentLanguageCode)
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

        avatarView = root.findViewById(R.id.current_avatar)
        avatarView.setImageResource(getAvatarID(avatarIndex))

        val modifyAvatarButton: MaterialButton = root.findViewById(R.id.modify_avatar_button)
        modifyAvatarButton.setOnClickListener {
            ModifyAvatarDialog.start(this, activity!!)
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

        confirmPasswordInput =
            root.findViewById<TextInputEditText>(R.id.password_reenter_input).apply {
                doAfterTextChanged {
                    val password = passwordInput.text.toString()
                    registerPresenter.validateConfirmPassword(password, text.toString())
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
            avatarIndex,
            emailInput.text.toString(),
            firstnameInput.text.toString(),
            lastnameInput.text.toString()
        )
    }

    override fun onAvatarChanged(avatarIndex: Int) {
        this.avatarIndex = avatarIndex
        avatarView.setImageResource(getAvatarID(avatarIndex))
        enableRegisterIfAllValid()
    }

    fun onRegisterSuccess() {
        val username = usernameInput.text.toString()
        MaterialAlertDialogBuilder(context)
            .setTitle("Registration completed")
            .setMessage("Account successfully created! Welcome, $username!")
            .setPositiveButton("Thanks!") { _, _ ->
                navigateTo(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
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

    override fun navigateTo(target: Class<*>, intentFlags: Int?) {
        if (activity == null)
            return

        val intent = Intent(activity, target)
        if (intentFlags != null) {
            intent.flags = intentFlags
        }
        startActivity(intent)
        activity?.finish()
    }

    /**
     * Changes if the "Register" button is enabled
     */
    private fun enableRegisterIfAllValid() {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()
        val email = emailInput.text.toString()
        val firstname = firstnameInput.text.toString()
        val lastname = lastnameInput.text.toString()

        // All fields are valid
        registerBtn.isEnabled = registerPresenter.validateUsername(username)
                && registerPresenter.validatePassword(password)
                && registerPresenter.validateConfirmPassword(password, confirmPassword)
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

    fun setConfirmPasswordError(error: String?) {
        confirmPasswordInput.error = error
    }

    fun changeResLanguage(language: String) {
        LocaleLanguageHelper.getLocalizedResources(context!!, language).apply {
            register_text.text = getString(R.string.register)
            modify_avatar_button.text = getString(R.string.modify)
            username_input_layout.hint = getString(R.string.login_username_hint)
            password_input_layout.hint = getString(R.string.login_password_hint)
            password_input_verify_layout.hint = getString(R.string.login_reenter_password_hint)
            email_input_layout.hint = getString(R.string.email_hint)
            name_input_layout.hint = getString(R.string.firstname)
            surname_input_layout.hint = getString(R.string.lastname)
            backBtn.text = getString(R.string.back)
            registerBtn.text = getString(R.string.register)
            all_required_text.text = getString(R.string.all_required)
        }
    }
}