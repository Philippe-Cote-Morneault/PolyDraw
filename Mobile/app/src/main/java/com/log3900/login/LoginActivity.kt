package com.log3900.login

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
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


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        createLoginFragment()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onBackPressed() {
        var current: Fragment? = supportFragmentManager.findFragmentByTag("REGISTER_FRAGMENT")
        if (current != null && current.isVisible) {
            println("OK! REGISTER")
            createLoginFragment()
        } else {
            current = supportFragmentManager.findFragmentByTag("LOGIN_FRAGMENT")
            if (current != null && current.isVisible) {
                println("OK! LOGIN")
            } else {
                // We're at the 'root', so we offer to exit
                createExitDialog()
            }
        }
    }

    private fun createLoginFragment() {
        val loginFragment = LoginFragment()
        val transaction = supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
            replace(R.id.card_view, loginFragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun createExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.quit_app)
            .setMessage(R.string.quit_app_confirm)
            .setPositiveButton("Exit") { _, _ -> finish() }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .setIcon(R.drawable.ic_exit_to_app_black_24dp)
            .show()
    }
}
