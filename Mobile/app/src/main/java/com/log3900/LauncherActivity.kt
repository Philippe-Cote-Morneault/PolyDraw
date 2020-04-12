package com.log3900

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.log3900.login.LoginActivity
import com.log3900.session.MonitoringService
import com.log3900.socket.SocketService


class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MainApplication.instance.startService(SocketService::class.java)
        MainApplication.instance.startService(MonitoringService::class.java)
        super.onCreate(savedInstanceState)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        MainApplication.instance.startService(SocketService::class.java)
        MainApplication.instance.startService(MonitoringService::class.java)
    }
}