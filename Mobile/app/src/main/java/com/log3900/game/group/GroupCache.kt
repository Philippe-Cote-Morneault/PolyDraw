package com.log3900.game.group

import java.util.*
import kotlin.collections.ArrayList

class GroupCache {
    private var groups: ArrayList<Group> = arrayListOf()

    fun addGroup(group: Group) {
        groups.add(group)
    }

    fun removeGroup(groupID: UUID) {
        groups.removeIf {
            it.ID == groupID
        }
    }

    fun getAllGroups(): ArrayList<Group> {
        return groups
    }

    fun addUserToGroup(groupID: UUID, userID: UUID, username: String) {
        val group = groups.find {
            it.ID == groupID
        }

        if (group != null) {
            group.players.add(userID)
        }
    }

    fun removeUserFromGroup(groupID: UUID, userID: UUID) {
        val group = groups.find {
            it.ID == groupID
        }

        if (group != null) {
            group.players.remove(userID)
        }
    }
}