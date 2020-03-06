package com.log3900.game.waiting_room

import com.log3900.game.group.Group
import com.log3900.game.group.Player

interface MatchWaitingRoomView {
    fun setPlayers(players: ArrayList<Player>)
    fun setGroup(group: Group)
}