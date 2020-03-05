package com.log3900.session

import com.log3900.MainActivity
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class NavigationManager {
    var currentActivity: MainActivity? = null

    constructor() {
        EventBus.getDefault().register(this)
    }

    private fun onGroupJoined() {
        currentActivity?.startNavigationFragment(R.id.navigation_main_match_waiting_room_fragment, null, true)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.GROUP_JOINED -> {
                onGroupJoined()
            }
        }
    }

    fun delete() {
        currentActivity = null
        EventBus.getDefault().unregister(this)
    }
}