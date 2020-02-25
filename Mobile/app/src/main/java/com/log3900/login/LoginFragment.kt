package com.log3900.login

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import com.log3900.utils.ui.KeyboardHelper

class LoginFragment : Fragment(), LoginView {
    // Services
    private val loginPresenter = LoginPresenter(this)
    // UI Elements
    private lateinit var loginButton: MaterialButton
    private lateinit var usernameTextInput: TextInputEditText
    private lateinit var usernameTextInputLayout: TextInputLayout
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var passwordTextInputLayout: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var registerButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        setupUIElements(root)
        return root
    }

    private fun setupUIElements(root: View) {
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
        KeyboardHelper.hideKeyboard(activity as Activity)

        loginPresenter.authenticate(usernameTextInput.text.toString(), passwordTextInput.text.toString())
    }

    private fun onRegisterButtonClick() {
        KeyboardHelper.hideKeyboard(activity as Activity)

        val newFragment: Fragment = RegisterFragment()
        val transaction: FragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)


        // Tag is used in LoginActivity to handle the back button
        transaction.replace(R.id.card_view, newFragment, "REGISTER_FRAGMENT")
        transaction.addToBackStack("LOGIN_FRAGMENT")    // This is probably unecessary

        // Commit the transaction
        transaction.commit()
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

    override fun showErrorDialog(title: String, message: String, positiveButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                                 negativeButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Retry", positiveButtonClickListener) //{ _, _ -> onLoginButtonClick() }
            .setNegativeButton("Cancel", negativeButtonClickListener)
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

    override fun closeView() {
        activity?.finishAffinity()
    }

    override fun onDestroy() {
        loginPresenter.destroy()
        super.onDestroy()
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
}