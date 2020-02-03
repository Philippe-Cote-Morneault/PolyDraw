package com.log3900

import android.content.Intent
import android.os.Bundle
import com.log3900.login.LoginActivity
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* TODO: token validation */

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
