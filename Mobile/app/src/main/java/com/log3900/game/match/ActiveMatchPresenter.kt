package com.log3900.game.match

import android.util.Log
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ActiveMatchPresenter : Presenter {
    private var activeMatchView: ActiveMatchView? = null
    private var matchManager: MatchManager

    constructor(activeMatchView: ActiveMatchView) {
        this.activeMatchView = activeMatchView
        this.matchManager = MatchManager()
        activeMatchView.setPlayers(matchManager.getCurrentMatch().players)
        subscribeToEvents()
        matchManager.notifyReadyToPlay()
    }

    private fun onPlayerTurnToDraw(playerTurnToDraw: PlayerTurnToDraw) {
        activeMatchView?.clearAllPlayerStatusRes()
        activeMatchView?.setPlayerStatus(playerTurnToDraw.userID, R.drawable.ic_edit_black)
    }

    private fun subscribeToEvents() {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.PLAYER_TURN_TO_DRAW -> onPlayerTurnToDraw(event.data as PlayerTurnToDraw)
        }
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        EventBus.getDefault().unregister(this)
        activeMatchView = null
    }
}