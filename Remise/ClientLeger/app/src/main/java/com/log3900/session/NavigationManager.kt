package com.log3900.session

import android.util.Log
import com.log3900.MainActivity
import com.log3900.R
import com.log3900.game.group.MatchMode
import com.log3900.game.match.Match
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

    private fun onMatchAboutToStart(match: Match) {
        Log.d("POTATO", "NavigationManager::onMatchAboutToStart()")
        if (currentActivity?.navigationController?.currentDestination?.id == R.id.navigation_main_match_waiting_room_fragment) {
            Log.d("POTATO", "NavigationManager::onMatchAboutToStart(), valid id and starting fragment")
            when (match.matchType) {
                MatchMode.FFA -> currentActivity?.startNavigationFragment(R.id.navigation_main_active_ffa_match_fragment, null, false)
                MatchMode.SOLO -> currentActivity?.startNavigationFragment(R.id.navigation_main_active_solo_match_fragment, null, false)
                MatchMode.COOP -> currentActivity?.startNavigationFragment(R.id.navigation_main_active_coop_match_fragment, null, false)
            }
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
            EventType.GROUP_LEFT -> {
                onGroupLeft()
            }
            EventType.GROUP_DELETED -> {
                onGroupLeft()
            }
            EventType.MATCH_ABOUT_TO_START -> {
                onMatchAboutToStart(event.data as Match)
            }
        }
    }

    fun delete() {
        currentActivity = null
        EventBus.getDefault().unregister(this)
    }
}