package com.log3900

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andremion.counterfab.CounterFab
import com.google.android.material.navigation.NavigationView
import com.log3900.chat.ChatManager
import com.log3900.login.LoginActivity
import com.log3900.profile.ProfileFragment
import com.log3900.settings.SettingsActivity
import com.log3900.settings.theme.ThemeManager
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent


import com.log3900.socket.SocketService
import com.log3900.tutorial.TutorialActivity
import com.log3900.user.account.AccountRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var hideShowMessagesFAB: CounterFab
    private lateinit var chatManager: ChatManager
    private lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ChatManager.getInstance()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    chatManager = it
                },
                {

                }
            )

        setupUI()

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        hideShowMessagesFAB = findViewById(R.id.hideShowMessage)
        hideShowMessagesFAB.setOnClickListener{
            var chatView = (supportFragmentManager.findFragmentById(R.id.activity_main_chat_fragment_container) as Fragment).view
            when(chatView!!.visibility){
                View.INVISIBLE -> {
                    chatManager.openChat()
                    chatView.visibility = View.VISIBLE
                }
                View.VISIBLE -> {
                    chatView.visibility = View.INVISIBLE
                    chatManager.closeChat()
                }
            }
        }


        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navigationController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_main_home_fragment,
                R.id.navigation_main_profile_fragment,
                R.id.navigation_main_draw_view_fragment
            ), drawerLayout)

        setupActionBarWithNavController(navigationController, appBarConfiguration)

        navView.setupWithNavController(navigationController)
        navView.menu.findItem(R.id.menu_item_activity_main_drawer_home).setOnMenuItemClickListener {
            navigationController.popBackStack(navigationController.currentDestination!!.id, true)
            navigationController.navigate(R.id.navigation_main_home_fragment)
            true
        }

        navView.menu.findItem(R.id.menu_item_activity_main_drawer_draw).setOnMenuItemClickListener {
            navigationController.popBackStack(navigationController.currentDestination!!.id, true)
            navigationController.navigate(R.id.navigation_main_draw_view_fragment)
            true
        }

        navView.menu.findItem(R.id.menu_item_activity_main_drawer_logout).setOnMenuItemClickListener {
            logout()
            true
        }

        navView.menu.findItem(R.id.menu_item_activity_main_drawer_profile).setOnMenuItemClickListener {
            navigationController.popBackStack(navigationController.currentDestination!!.id, true)
            navigationController.navigate(R.id.navigation_main_profile_fragment)
            true
        }

        // Header
        val header = navView.getHeaderView(0)
        val avatar: ImageView = header.findViewById(R.id.nav_header_avatar)
        avatar.setOnClickListener {
            startFragment("PROFILE_VIEW_FRAGMENT_TAG", ProfileFragment())
        }

        if (!AccountRepository.getInstance().getAccount().tutorialDone) {
            startActivity(Intent(applicationContext, TutorialActivity::class.java))
        }

        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        if (ThemeManager.hasActivityThemeChanged(this)) {
            this.recreate()
            chatManager.openChat()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigationController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.app_bar_main_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageView>(R.id.app_bar_main_tutorial).setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }
    }

    private fun logout() {
        SocketService.instance?.disconnectSocket(Handler {
            intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            EventBus.getDefault().post(MessageEvent(EventType.LOGOUT, ""))
            true
        })
    }

    //override fun onBackPressed() {
    //    super.onBackPressed()
        //logout()
    //}

    private fun startFragment(tag: String, fragment: Fragment) {
        drawerLayout.closeDrawer(GravityCompat.START)

        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) != null)
            return

        fragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment, tag)
            commit()
        }
    }

    private fun onUnreadMessagesChanged(unreadMessagesCount: Int) {
        hideShowMessagesFAB.count = unreadMessagesCount
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.UNREAD_MESSAGES_CHANGED -> {
                onUnreadMessagesChanged(event.data as Int)
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}
