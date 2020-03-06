package com.log3900

import android.app.Application
import android.content.Intent
import android.view.MenuItem
import com.log3900.session.MonitoringService
import com.log3900.session.NavigationManager
import com.log3900.socket.SocketService

class MainApplication : Application() {
    var mainActivity: MainActivity? = null
    private var navigationManager: NavigationManager = NavigationManager()
    companion object {
        lateinit var instance: MainApplication
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        startService(SocketService::class.java)
        startService(MonitoringService::class.java)
    }

    fun startFragment(destinationID: Int, menuItem: MenuItem?, keepBackstack: Boolean) {
        mainActivity?.startNavigationFragment(destinationID, menuItem, keepBackstack)
    }

    fun startService(service: Class<*>) {
        startService(Intent(this, service))
    }

    fun stopService(service: Class<*>) {
        stopService(Intent(this, service))
    }

    fun registerMainActivity(activity: MainActivity) {
        mainActivity = activity
        navigationManager.currentActivity = activity
    }

    fun unregisterMainActivity() {
        navigationManager.currentActivity = null
        mainActivity = null
    }

}