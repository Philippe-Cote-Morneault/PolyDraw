package com.log3900

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.log3900.draw.DrawViewFragment
import com.log3900.login.LoginActivity
import com.log3900.profile.ProfileActivity
import com.log3900.profile.ProfileFragment
import com.log3900.socket.Event
import com.log3900.socket.SocketEvent


import com.log3900.socket.SocketService
import com.log3900.ui.home.HomeFragment

open class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val hideShowMessage: FloatingActionButton = findViewById(R.id.hideShowMessage)
        hideShowMessage.setOnClickListener{ _ ->

            var chatView = (supportFragmentManager.findFragmentById(R.id.fragment) as Fragment).view
            when(chatView!!.visibility){
                View.INVISIBLE -> chatView.visibility = View.VISIBLE
                View.VISIBLE -> chatView.visibility = View.INVISIBLE
            }
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
        navView.menu.findItem(R.id.nav_home).setOnMenuItemClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startFragment("HOME_VIEW_FRAGMENT") { HomeFragment() }
            true
        }

        navView.menu.findItem(R.id.nav_draw).setOnMenuItemClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startFragment("DRAW_VIEW_FRAGMENT") { DrawViewFragment() }
            true
        }

        navView.menu.findItem(R.id.logoutButton).setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.logoutButton -> logout()
            }
            true
        }

        navView.menu.findItem(R.id.nav_profile).setOnMenuItemClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startFragment("PROFILE_VIEW_FRAGMENT_TAG") { ProfileFragment() }
            true
        }

        // Header
        val header = navView.getHeaderView(0)
        val avatar: ImageView = header.findViewById(R.id.nav_header_avatar)
        avatar.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startFragment("PROFILE_VIEW_FRAGMENT_TAG") { ProfileFragment() }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout() {
        SocketService.instance?.disconnectSocket(Handler {
            intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            true
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        logout()
    }

    private fun<T: Fragment> startFragment(tag: String, fragmentConstructor: () -> T) {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) != null)
            return

        val fragment: Fragment = fragmentConstructor()
        fragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment, fragment, tag)
            commit()
        }
    }
}
