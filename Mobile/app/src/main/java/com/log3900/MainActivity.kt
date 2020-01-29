package com.log3900

import android.content.Intent
import android.os.Bundle
import com.log3900.login.LoginActivity
import androidx.appcompat.app.AppCompatActivity
import com.log3900.socketServices.SocketService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* TODO: token validation */

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
