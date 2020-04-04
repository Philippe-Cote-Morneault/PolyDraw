package com.log3900.login

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.login.register.RegisterFragment
import com.log3900.session.MonitoringService
import com.log3900.socket.SocketService

private const val REGISTER_FRAGMENT_TAG = "REGISTER_FRAGMENT"
private const val LOGIN_FRAGMENT_TAG = "LOGIN_FRAGMENT"

class LoginActivity : AppCompatActivity() {
    private lateinit var languageButton: MaterialButton
    private var currentLanguageCode = MainApplication.instance.getContext().resources.configuration.locales.get(0).language.also { Log.d("LANGUAGE", "Start: $it") }

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApplication.instance.startService(SocketService::class.java)
        MainApplication.instance.startService(MonitoringService::class.java)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setUpUI()
        createLoginFragment()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun setUpUI() {
        setUpLanguageButton()
    }

    private fun setUpLanguageButton() {
        languageButton = findViewById(R.id.login_language_button)
        languageButton.apply {
            text = getTextFromLanguageCode()

            setOnClickListener {
                swapLanguageCode()
                updateLanguage()
                text = getTextFromLanguageCode()
                Log.d("LANGUAGE", currentLanguageCode)
//                supportFragmentManager.findFragmentByTag("LOGIN_FRAGMENT")?.

//                recreate()
//                LanguagePickerDialog().show(supportFragmentManager, "language_dialog")
            }
        }
    }

    private fun getTextFromLanguageCode(): String {
        return if (currentLanguageCode == "en") {
            "Français"
        } else {
            "English"
        }
    }

    private fun swapLanguageCode() {
        Log.d("LANGUAGE", "CURRENT: $currentLanguageCode")
        currentLanguageCode = if (currentLanguageCode == "en") {
            "fr"
        } else {
            "en"
        }
    }

    private fun updateLanguage() {
        changeFragmentLanguage()
//        val locale = Locale(currentLanguageCode)
//        Locale.setDefault(locale)
//
//        val config = MainApplication.instance.getContext().resources.configuration
//        config.setLocale(locale)
//
//        MainApplication.instance.getContext().createConfigurationContext(config)

//        val locale = Locale(currentLanguageCode)
//        Locale.setDefault(locale)
//
//        val config = Configuration()
//        config.setLocale(locale)
//        MainApplication.instance.getContext().resources.updateConfiguration(config,
//            MainApplication.instance.resources.displayMetrics
//        )
    }

    override fun onResume() {
        super.onResume()
    }

    private fun changeFragmentLanguage() {
        Log.d("LANGUAGE", "All frags: ${supportFragmentManager.fragments}")
        var currentFragment = supportFragmentManager.findFragmentByTag(REGISTER_FRAGMENT_TAG)
        if (currentFragment != null && currentFragment is RegisterFragment && currentFragment.isVisible) {
//            currentFragment
            Log.d("LANGUAGE", "Found register")
        } else {
            currentFragment = supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG)
            if (currentFragment != null && currentFragment is LoginFragment && currentFragment.isVisible) {
                currentFragment.changeResLanguage(currentLanguageCode)
                Log.d("LANGUAGE", "Found login")
            }
        }
        Log.d("LANGUAGE", "current fragment is $currentFragment")
    }

    override fun onBackPressed() {
        var current: Fragment? = supportFragmentManager.findFragmentByTag(REGISTER_FRAGMENT_TAG)
        if (current != null && current.isVisible) {
            Log.d("LANGUAGE", "OK! REGISTER")
            createLoginFragment()
        } else {
            current = supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG)
            if (current != null && current.isVisible) {
                Log.d("LANGUAGE", "OK! LOGIN: ${current is LoginFragment}")
                createExitDialog()
            } else {
                // We're at the 'root', so we offer to exit
//                createExitDialog()
            }
        }
    }

    private fun createLoginFragment() {
        val loginFragment = LoginFragment()
        val transaction = supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
            replace(R.id.card_view, loginFragment, LOGIN_FRAGMENT_TAG)
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
