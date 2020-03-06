package com.log3900.game.group

import java.util.*
import kotlin.collections.ArrayList

class GroupCache {
    private var groups: ArrayList<Group> = arrayListOf()
    var needsReload: Boolean = true

    fun setGroups(newGroups: ArrayList<Group>) {
        groups.clear()
        newGroups.forEach {
            groups.add(it)
        }
    }

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

    fun getGroup(groupID: UUID): Group? {
        return groups.find { it.ID == groupID }
    }

    fun containsGroup(groupID: UUID): Boolean {
        return groups.find { it.ID == groupID} != null
    }

    fun addUserToGroup(groupID: UUID, player: Player) {
        val group = groups.find {
            it.ID == groupID
        }

        if (group != null) {
            if (group.players.find { it.ID == player.ID } == null) {
                group.players.add(player)
            }
        }
    }

    fun removeUserFromGroup(groupID: UUID, userID: UUID) {
        val group = groups.find {
            it.ID == groupID
        }

        if (group != null) {
            group.players.removeIf {
                it.ID == userID
            }
        }
    }
}