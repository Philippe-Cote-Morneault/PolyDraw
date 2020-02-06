package com.log3900

import android.content.Intent
import android.os.Bundle
import com.log3900.login.LoginActivity
import androidx.appcompat.app.AppCompatActivity
import com.log3900.socket.SocketService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // TODO: link to logout button
    private fun logout() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        // TODO: Reset views? Needs test
        SocketService.instance.disconnect()
        finish()
    }
}
