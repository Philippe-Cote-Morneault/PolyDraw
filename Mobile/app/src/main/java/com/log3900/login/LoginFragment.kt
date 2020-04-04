package com.log3900.login

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.log3900.R
import com.log3900.login.register.RegisterFragment
import com.log3900.settings.LocaleLanguageHelper
import com.log3900.utils.ui.KeyboardHelper
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

class LoginFragment : Fragment(), LoginView {
    // Services
    private var loginPresenter: LoginPresenter? = null
    // UI Elements
    private lateinit var contentLayout: ConstraintLayout
    private lateinit var loginButton: MaterialButton
    private lateinit var usernameTextInput: TextInputEditText
    private lateinit var usernameTextInputLayout: TextInputLayout
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var passwordTextInputLayout: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var registerButton: MaterialButton
    private lateinit var rememberMeCheckBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        setupUIElements(root)

        loginPresenter = LoginPresenter(this)

        return root
    }

    private fun setupUIElements(root: View) {
        contentLayout = root.findViewById(R.id.card_content_layout)

        loginButton = root.findViewById(R.id.activity_login_login_button)
        loginButton.setOnClickListener {
            onLoginButtonClick()
        }

        usernameTextInput = root.findViewById(R.id.activity_login_text_input_username)
        usernameTextInput.doAfterTextChanged {
            onUsernameChange()
        }
        passwordTextInput = root.findViewById(R.id.activity_login_text_input_password)
        passwordTextInput.doAfterTextChanged {
            onPasswordChange()
        }

        usernameTextInputLayout = root.findViewById(R.id.activity_login_text_input_layout_username)
        passwordTextInputLayout = root.findViewById(R.id.activity_login_text_input_layout_password)

        progressBar = root.findViewById(R.id.activity_login_progressbar_login)

        registerButton = root.findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            onRegisterButtonClick()
        }

        rememberMeCheckBox = root.findViewById(R.id.activity_login_remember_me_checkbox)
    }

    private fun onUsernameChange() {
        loginPresenter?.validateUsername(usernameTextInput.text.toString())
    }

    private fun onPasswordChange() {
        loginPresenter?.validatePassword(passwordTextInput.text.toString())
    }

    override fun onResume() {
        super.onResume()
        loginPresenter?.resume()
    }

    private fun onLoginButtonClick() {
        KeyboardHelper.hideKeyboard(activity as Activity)

        if (rememberMeCheckBox.isChecked)
            loginPresenter?.rememberUser()

        loginPresenter?.authenticate(usernameTextInput.text.toString(), passwordTextInput.text.toString())
    }

    private fun onRegisterButtonClick() {
        KeyboardHelper.hideKeyboard(activity as Activity)

        val newFragment: Fragment = RegisterFragment()
        val transaction: FragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)


        // Tag is used in LoginActivity to handle the back button
        transaction.replace(R.id.card_view, newFragment, "REGISTER_FRAGMENT")
        transaction.addToBackStack(null)

        // Commit the transaction
        transaction.commit()
    }

    private fun isRememberMeChecked(): Boolean = rememberMeCheckBox.isChecked

    override fun showWelcomeBackMessage(username: String) {
        Toast.makeText(context, "Welcome back, $username", Toast.LENGTH_LONG).show()
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
//        usernameTextInputLayout.error = error
    }

    override fun setPasswordError(error: String) {
//        passwordTextInputLayout.error = error
    }

    override fun clearPasswordError() {
        passwordTextInputLayout.error = null
    }

    override fun clearUsernameError() {
        usernameTextInputLayout.error = null
    }

    override fun showErrorDialog(title: String, message: String, positiveButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                                 negativeButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", positiveButtonClickListener)
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun showProgressDialog(dialog: DialogFragment) {
        dialog.show(activity!!.supportFragmentManager, "progressDialog")
    }

    override fun hideProgressDialog(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun enableView() {
        contentLayout.children.iterator().forEach {
            it.isEnabled = true
        }
        registerButton.isEnabled = true
    }

    override fun disableView() {
        contentLayout.children.iterator().forEach {
            it.isEnabled = false
        }
        registerButton.isEnabled = false
    }

    override fun closeView() {
        activity?.finishAffinity()
    }

    override fun onDestroy() {
        loginPresenter?.destroy()
        super.onDestroy()
        loginPresenter = null
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

    fun changeResLanguage(language: String) {
        loginButton.text = getString(R.string.login_button)
        LocaleLanguageHelper.getLocalizedResources(context!!, language).apply {
            activity_login_text_view_welcome.text = getString(R.string.login_welcome)
            activity_login_text_input_layout_username.hint = getString(R.string.login_username_hint)
            activity_login_text_input_layout_password.hint = getString(R.string.login_password_hint)
            activity_login_remember_me_checkbox.text = getString(R.string.login_remember_me)
            register_question_text.text = getString(R.string.login_signup_question)
            registerButton.text = getString(R.string.login_signup_now)
        }

//        activity_login_text_view_welcome.text = language
    }
}