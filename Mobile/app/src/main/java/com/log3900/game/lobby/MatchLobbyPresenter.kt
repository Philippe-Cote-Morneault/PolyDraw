package com.log3900.game.lobby

import android.util.Log
import com.log3900.game.group.Group
import com.log3900.game.group.GroupCreated
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.architecture.Presenter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MatchLobbyPresenter : Presenter {
    private var matchLobbyView: MatchLobbyView? = null
    private var groupManager: GroupManager? = null

    constructor(matchLobbyView: MatchLobbyView) {
        this.matchLobbyView = matchLobbyView

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
        groupManager?.getAvailableGroups()?.subscribe(
            {
                matchLobbyView?.setAvailableGroups(it)
            },
            {

            }
        )

        subscribeToEvents()
    }


    fun onCreateMatchClicked() {
        matchLobbyView?.showMatchCreationDialog()
    }

    fun onCreateMatch(createdGroup: GroupCreated) {
        groupManager?.createGroup(createdGroup)
    }

    fun onJoinMatchClick(group: Group) {
        groupManager?.joinGroup(group)
    }

    private fun onGroupCreated(group: Group) {
        matchLobbyView?.notifyMatchesChanged()
    }
    private fun onGroupDeleted(groupID: UUID) {
        matchLobbyView?.notifyMatchesChanged()
    }

    private fun subscribeToEvents() {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when(event.type) {
            EventType.GROUP_CREATED -> {
                onGroupCreated(event.data as Group)
            }
            EventType.GROUP_DELETED -> {
                onGroupDeleted(event.data as UUID)
            }
        }
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        matchLobbyView = null
    }
}