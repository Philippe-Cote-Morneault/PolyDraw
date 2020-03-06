package com.log3900.game.waiting_room

import com.log3900.chat.Channel.Channel
import com.log3900.chat.ChatMessage
import com.log3900.game.group.GroupManager
import com.log3900.game.group.Player
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import com.log3900.user.account.AccountRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

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
        matchWaitingRoomView?.setGroup(groupManager?.currentGroup!!)
        matchWaitingRoomView?.setPlayers(groupManager?.currentGroup?.players!!)

        if (groupManager?.currentGroup?.ownerID == AccountRepository.getInstance().getAccount().ID) {
            matchWaitingRoomView?.displayStartMatchButton(true)
        } else {
            matchWaitingRoomView?.displayStartMatchButton(false)
        }

        EventBus.getDefault().register(this)
    }

    fun onLeaveMatchClick() {
        groupManager?.leaveCurrentGroup()
    }

    fun onPlayerClick(player: Player) {
        // Find something interesting to do here
    }

    fun onGroupUpdated(groupID: UUID){
        if (groupID == groupManager?.currentGroup?.ID) {
            matchWaitingRoomView?.notifyPlayyersChanged()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.GROUP_UPDATED -> {
                onGroupUpdated(event.data as UUID)
            }

        }
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        groupManager?.leaveCurrentGroup()
        matchWaitingRoomView = null
        groupManager = null
        EventBus.getDefault().unregister(this)
    }
}