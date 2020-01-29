package com.log3900

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.log3900.login.LoginActivity
import androidx.appcompat.app.AppCompatActivity
import com.log3900.chat.ChatFragment
import com.log3900.socketServices.SocketService
import kotlinx.android.synthetic.main.activity_main.*

class PolyDraw : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: PolyDraw? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // initialize for any

        // Use ApplicationContext.
        // example: SharedPreferences etc...
        // TODO: Check bearer token

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
