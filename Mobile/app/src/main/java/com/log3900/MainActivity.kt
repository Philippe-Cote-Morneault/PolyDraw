package com.log3900

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.log3900.socketServices.SocketService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
