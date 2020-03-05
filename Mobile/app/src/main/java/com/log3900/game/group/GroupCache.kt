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
}