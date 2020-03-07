package com.log3900

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.log3900.login.LoginActivity


class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}