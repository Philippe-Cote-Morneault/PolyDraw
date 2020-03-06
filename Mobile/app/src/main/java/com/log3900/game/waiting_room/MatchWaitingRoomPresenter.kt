package com.log3900.game.waiting_room

import com.log3900.game.group.GroupManager
import com.log3900.game.group.Player
import com.log3900.shared.architecture.Presenter
import io.reactivex.android.schedulers.AndroidSchedulers

class MatchWaitingRoomPresenter : Presenter {
    private var matchWaitingRoomView: MatchWaitingRoomView? = null
    private var groupManager: GroupManager? = null

    constructor(matchWaitingRoomView: MatchWaitingRoomView) {
        this.matchWaitingRoomView = matchWaitingRoomView

        GroupManager.getInstance()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    groupManager = it
                    init()
                },
                {

                }
            )
    }

    private fun init() {
        matchWaitingRoomView?.setPlayers(groupManager?.currentGroup?.players!!)
    }

    fun onLeaveMatchClick() {
        groupManager?.leaveCurrentGroup()
    }

    fun onPlayerClick(player: Player) {
        // Find something interesting to do here
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        groupManager?.leaveCurrentGroup()
        matchWaitingRoomView = null
        groupManager = null
    }
}