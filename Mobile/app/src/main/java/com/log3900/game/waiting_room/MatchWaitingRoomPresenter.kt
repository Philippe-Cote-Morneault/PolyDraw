package com.log3900.game.waiting_room

import com.log3900.chat.Channel.Channel
import com.log3900.chat.ChatMessage
import com.log3900.game.group.GroupManager
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import com.log3900.game.group.UserLeftGroup
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

    fun onStartMatchClick() {
        groupManager?.startMatch()
    }

    fun onPlayerClick(player: Player) {
        // Find something interesting to do here
    }

    fun onPlayerAddClick() {
        groupManager?.addVirtualPlayers(1)
    }

    fun onPlayerRemoveClick(player: Player) {
        groupManager?.kickPlayer(player)
    }

    private fun onGroupUpdated(groupID: UUID){
        if (groupID == groupManager?.currentGroup?.ID) {
            matchWaitingRoomView?.notifyGroupUpdated(groupManager?.currentGroup!!)
        }
    }

    private fun onPlayerJoined(groupID: UUID, playerID: UUID) {
        if (groupID == groupManager?.currentGroup?.ID) {
            matchWaitingRoomView?.notifyPlayerJoined(playerID)
            matchWaitingRoomView?.notifyGroupUpdated(groupManager?.currentGroup!!)
        }
    }

    private fun onPlayerLeft(userLeftGroup: UserLeftGroup) {
        if (userLeftGroup.groupID == groupManager?.currentGroup?.ID) {
            matchWaitingRoomView?.notifyPlayerLeft(userLeftGroup.userID)
            matchWaitingRoomView?.notifyGroupUpdated(groupManager?.currentGroup!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.GROUP_UPDATED -> {
                onGroupUpdated(event.data as UUID)
            }
            EventType.PLAYER_JOINED_GROUP -> {
                val eventData = event.data as Pair<UUID, UUID>
                onPlayerJoined(eventData.first, eventData.second)
            }
            EventType.PLAYER_LEFT_GROUP -> {
                val eventData = event.data as UserLeftGroup
                onPlayerLeft(eventData)
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