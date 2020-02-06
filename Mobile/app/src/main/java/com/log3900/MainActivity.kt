package com.log3900

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.log3900.login.LoginActivity


import com.log3900.socket.SocketService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val hideShowMessage: FloatingActionButton = findViewById(R.id.hideShowMessage)
        hideShowMessage.setOnClickListener{ view ->
            Snackbar.make(view, "Hide/Show Message", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
        navView.menu.findItem(R.id.logoutButton).setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.logoutButton -> logout()
            }
            true
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        SocketService.instance.disconnect()
        finish()
    }

}
