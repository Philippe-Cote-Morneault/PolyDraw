package com.log3900.session

import com.log3900.MainActivity
import com.log3900.R
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NavigationManager {
    var currentActivity: MainActivity? = null

    constructor() {
        EventBus.getDefault().register(this)
    }

    private fun onGroupJoined() {
        currentActivity?.startNavigationFragment(R.id.navigation_main_match_waiting_room_fragment, null, true)
    }

    private fun onGroupLeft() {
        if (currentActivity?.navigationController?.currentDestination?.id == R.id.navigation_main_match_waiting_room_fragment) {
            currentActivity?.navigateBack()
        }
    }

    private fun onMatchAboutToStart() {
        if (currentActivity?.navigationController?.currentDestination?.id == R.id.navigation_main_match_waiting_room_fragment) {
            currentActivity?.startNavigationFragment(R.id.navigation_main_active_match_fragment, null, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.GROUP_JOINED -> {
                onGroupJoined()
            }
            EventType.LEAVE_GROUP -> {
                onGroupLeft()
            }
            EventType.GROUP_DELETED -> {
                onGroupLeft()
            }
            EventType.MATCH_ABOUT_TO_START -> {
                onMatchAboutToStart()
            }
        }
    }

    fun delete() {
        currentActivity = null
        EventBus.getDefault().unregister(this)
    }
}