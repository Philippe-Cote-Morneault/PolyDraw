package com.log3900.game.lobby

import com.log3900.game.group.Group
import java.util.*

interface MatchLobbyView {
    fun showMatchCreationDialog()
    fun setAvailableGroups(groups: ArrayList<Group>)
    fun notifyMatchesChanged()
    fun groupUpdated(groupID: UUID)
}