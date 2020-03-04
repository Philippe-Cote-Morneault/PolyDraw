package com.log3900.game.lobby

import com.log3900.game.group.Group
import com.log3900.game.group.GroupCreated
import com.log3900.shared.architecture.Presenter
import io.reactivex.android.schedulers.AndroidSchedulers

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

    }


    fun onCreateMatchClicked() {
        matchLobbyView?.showMatchCreationDialog()
    }

    fun onCreateMatch(createdGroup: GroupCreated) {
        groupManager?.createGroup(createdGroup)
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        matchLobbyView = null
    }
}