package com.log3900.game.lobby

import com.log3900.game.group.Group

interface MatchLobbyView {
    fun showMatchCreationDialog()
    fun setAvailableGroups(groups: ArrayList<Group>)
}