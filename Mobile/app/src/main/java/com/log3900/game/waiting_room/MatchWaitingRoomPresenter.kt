package com.log3900.game.waiting_room

import com.log3900.chat.ChatManager
import com.log3900.game.group.GroupManager
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

    }

    fun onLeaveMatchClick() {
        groupManager?.leaveCurrentGroup()
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        matchWaitingRoomView = null
    }
}