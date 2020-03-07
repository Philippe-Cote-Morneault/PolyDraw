package com.log3900.shared.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import com.log3900.login.LoginActivity
import com.log3900.settings.theme.ThemeManager

class ErrorDialog : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this)
            .setTitle("Connection Error")
            .setMessage("You will be redirected to the login page.")
            .setPositiveButton("OK") {dialog, which ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}