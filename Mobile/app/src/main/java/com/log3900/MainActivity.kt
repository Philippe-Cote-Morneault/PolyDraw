package com.log3900


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andremion.counterfab.CounterFab
import com.google.android.material.navigation.NavigationView
import com.log3900.chat.ChatManager
import com.log3900.login.LoginActivity
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
    lateinit var navigationController: NavController
    private lateinit var navigationView: NavigationView
    private lateinit var toolbarContainer: LinearLayout

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

        val toolbar= findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbarContainer = findViewById(R.id.app_bar_main_toolbar_content_container)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_main_match_lobby_fragment,
                R.id.navigation_main_profile_fragment,
                R.id.navigation_main_draw_view_fragment
            ), drawerLayout)

        setupActionBarWithNavController(navigationController, appBarConfiguration)

        navigationView.setupWithNavController(navigationController)

        switchToolbar(R.layout.toolbar_activity_main)

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



        setupUI()

        navigationController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.navigation_main_active_match_fragment) {
                switchToolbar(R.layout.toolbar_active_match)
            } else {
                switchToolbar(R.layout.toolbar_activity_main)
            }
        }

        navigationView.menu.findItem(R.id.menu_item_activity_main_drawer_logout).setOnMenuItemClickListener {
            logout()
            true
        }

        // Header
        val header = navigationView.getHeaderView(0)
        val avatar: ImageView = header.findViewById(R.id.nav_header_avatar)
        avatar.setOnClickListener {
            startNavigationFragment(
                R.id.navigation_main_profile_fragment,
                navigationView.menu.findItem(R.id.navigation_main_profile_fragment),
                false
            )
        }

        if (!AccountRepository.getInstance().getAccount().tutorialDone) {
            startActivity(Intent(applicationContext, TutorialActivity::class.java))
        }

        MainApplication.instance.registerMainActivity(this)

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

    fun startNavigationFragment(destinationID: Int, menuItem: MenuItem?, keepBackstack: Boolean = true) {
        if (!keepBackstack) {
            navigationController.popBackStack()
        }
        navigationController.navigate(destinationID)
    }

    fun navigateBack() {
        navigationController.navigateUp()
    }

    fun switchToolbar(layout: Int) {
        toolbarContainer.removeAllViews()
        val newToolbarView = layoutInflater.inflate(layout, null)
        newToolbarView.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        toolbarContainer.addView(newToolbarView)

        if (layout == R.layout.toolbar_active_match) {
            supportActionBar?.title = ""
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
        MainApplication.instance.unregisterMainActivity()
        super.onDestroy()
    }
}
