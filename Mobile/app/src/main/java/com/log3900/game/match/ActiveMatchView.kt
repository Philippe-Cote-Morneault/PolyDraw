package com.log3900.game.match

import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

interface ActiveMatchView {
    fun setPlayers(players: ArrayList<Player>)
    fun setPlayerStatus(playerID: UUID, statusImageRes: Int)
    fun clearAllPlayerStatusRes()
}