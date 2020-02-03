package com.log3900

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import com.log3900.socket.SocketHandler
import com.log3900.socket.SocketService
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.concurrent.CountDownLatch


class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, SocketService::class.java))
        val intent = Intent(this, MainActivity::class.java)// New activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}