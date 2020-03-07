package com.log3900.game.waiting_room

import com.log3900.game.group.Group
import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

interface MatchWaitingRoomView {
    fun setPlayers(players: ArrayList<Player>)
    fun setGroup(group: Group)
    fun displayStartMatchButton(display: Boolean)
    fun enableStartMatchButton(enable: Boolean)
    fun notifyGroupUpdated()
    fun notifyPlayerJoined(playerID: UUID)
    fun notifyPlayerLeft(playerID: UUID)
}