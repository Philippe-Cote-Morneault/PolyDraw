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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
import com.log3900.session.MonitoringService
import com.log3900.settings.SettingsActivity
import com.log3900.settings.language.LanguageManager
import com.log3900.settings.sound.SoundManager
import com.log3900.settings.theme.ThemeManager
import com.log3900.shared.architecture.DialogEventMessage
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.ui.dialogs.SimpleConfirmationDialog
import com.log3900.shared.ui.dialogs.SimpleErrorDialog
import com.log3900.socket.SocketService
import com.log3900.tutorial.TutorialActivity
import com.log3900.user.account.AccountRepository
import com.log3900.utils.ui.getAccountAvatarID
import com.log3900.utils.ui.getAvatarID
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var hideShowMessagesFAB: CounterFab
    private lateinit var toggleSoundEffectsButton: ImageView
    private lateinit var chatManager: ChatManager
    lateinit var navigationController: NavController
    private lateinit var navigationView: NavigationView
    private lateinit var toolbarContainer: LinearLayout
    private lateinit var chatOuterContainer: LinearLayout
    private var isChatOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApplication.instance.startService(SocketService::class.java)
        MainApplication.instance.startService(MonitoringService::class.java)
        ThemeManager.applyTheme(this)
        LanguageManager.applySavedLanguage(baseContext)
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

        toolbarContainer = findViewById<LinearLayout>(R.id.app_bar_main_toolbar_content_container)
        chatOuterContainer = findViewById(R.id.activity_main_chat_fragment_outer_container)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_main_match_lobby_fragment,
                R.id.navigation_main_profile_fragment
//                R.id.navigation_main_draw_view_fragment
            ), drawerLayout)

        setupActionBarWithNavController(navigationController, appBarConfiguration)

        navigationView.setupWithNavController(navigationController)

        switchToolbar(R.layout.toolbar_activity_main)

        hideShowMessagesFAB = findViewById(R.id.hideShowMessage)
        hideShowMessagesFAB.setOnClickListener{
            if (isChatOpen) {
                closeChat()
            } else {
                openChat()
            }
        }

        toggleSoundEffectsButton = findViewById(R.id.app_bar_main_image_view_volume)
        toggleSoundEffectsButton.setOnClickListener {
            SoundManager.toggleSoundEffect(!SoundManager.areSoundEffectsEnabled()).subscribe {
                setSoundEffectsIcon(SoundManager.areSoundEffectsEnabled())
            }
        }

        setSoundEffectsIcon(SoundManager.areSoundEffectsEnabled())

        setupUI()

        navigationController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.navigation_main_active_ffa_match_fragment -> {
                    switchToolbar(R.layout.toolbar_active_ffa_match)
                    openChat()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                R.id.navigation_main_active_solo_match_fragment -> {
                    switchToolbar(R.layout.toolbar_active_solo_match)
                    openChat()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                R.id.navigation_main_active_coop_match_fragment -> {
                    switchToolbar(R.layout.toolbar_active_coop_match)
                    openChat()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                R.id.navigation_main_match_waiting_room_fragment -> {
                    openChat()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                else -> {
                    switchToolbar(R.layout.toolbar_activity_main)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        }

        navigationView.menu.findItem(R.id.menu_item_activity_main_drawer_logout).setOnMenuItemClickListener {
            logout()
            true
        }

        // Header
        val header = navigationView.getHeaderView(0)
        val currentAccount = AccountRepository.getInstance().getAccount()
        val avatar: ImageView = header.findViewById(R.id.nav_header_avatar)
        avatar.setImageResource(getAvatarID(currentAccount.pictureID))
        avatar.setOnClickListener {
            startNavigationFragment(
                R.id.navigation_main_profile_fragment,
                navigationView.menu.findItem(R.id.navigation_main_profile_fragment),
                false
            )
        }
        val username = header.findViewById<TextView>(R.id.nav_header_username)
        username.text = currentAccount.username

        if (!AccountRepository.getInstance().getAccount().tutorialDone) {
            startActivity(Intent(applicationContext, TutorialActivity::class.java))
        }

        MainApplication.instance.registerMainActivity(this)

        EventBus.getDefault().register(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navigationController.currentDestination?.id == R.id.navigation_main_match_lobby_fragment) {
                    showLogoutDialog()
                } else {
                    navigationController.navigateUp()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (ThemeManager.hasActivityThemeChanged(this) || LanguageManager.hasContextLanguageChanged(baseContext)) {
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::chatManager.isInitialized) {
            closeChat()
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

    fun showLogoutDialog() {
        SimpleConfirmationDialog(
            this,
            getString(R.string.logout),
            getString(R.string.logout_lobby_confirm),
            {_, _-> logout()},
            null
        ).show()
    }

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

        if (layout == R.layout.toolbar_active_ffa_match || layout == R.layout.toolbar_active_coop_match || layout == R.layout.toolbar_active_solo_match) {
            supportActionBar?.title = ""
        } else {
            setupUI()
        }
    }

    private fun onUnreadMessagesChanged(unreadMessagesCount: Int) {
        hideShowMessagesFAB.count = unreadMessagesCount
    }

    private fun onShowErrorMessage(message: DialogEventMessage) {
        SimpleErrorDialog(this, message.title, message.message, message.positiveButtonListener, message.negativeButtonListener).show()
    }

    private fun onCurrentUserUpdated() {
        // Update nav header
        val header = navigationView.getHeaderView(0)
        val currentAccount = AccountRepository.getInstance().getAccount()
        val avatar: ImageView = header.findViewById(R.id.nav_header_avatar)
        avatar.setImageResource(getAvatarID(currentAccount.pictureID))

        val username = header.findViewById<TextView>(R.id.nav_header_username)
        username.text = currentAccount.username
    }

    private fun setSoundEffectsIcon(enabled: Boolean) {
        if (enabled) {
            toggleSoundEffectsButton.setImageResource(R.drawable.ic_volume_up_black)
        } else {
            toggleSoundEffectsButton.setImageResource(R.drawable.ic_volume_off_black)
        }
    }

    fun openChat() {
        if (!isChatOpen) {
            //val chatView = (supportFragmentManager.findFragmentById(R.id.activity_main_chat_fragment_container) as Fragment).view
            chatManager.openChat()
            chatOuterContainer.visibility = View.VISIBLE
            isChatOpen = true
        }
    }

    fun closeChat() {
        if (isChatOpen) {
            //val chatView = (supportFragmentManager.findFragmentById(R.id.activity_main_chat_fragment_container) as Fragment).view
            chatOuterContainer.visibility = View.INVISIBLE
            chatManager.closeChat()
            isChatOpen = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.UNREAD_MESSAGES_CHANGED -> {
                onUnreadMessagesChanged(event.data as Int)
            }
            EventType.SHOW_ERROR_MESSAGE -> {
                onShowErrorMessage(event.data as DialogEventMessage)
            }
            EventType.CURRENT_USER_UPDATED -> {
                onCurrentUserUpdated()
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        MainApplication.instance.unregisterMainActivity(this)
        super.onDestroy()
    }
}
