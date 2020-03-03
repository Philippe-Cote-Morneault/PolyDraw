package com.log3900.game.group

import com.squareup.moshi.Json
import java.util.*
import kotlin.collections.ArrayList

class Group(@Json(name = "GroupID") var groupID: UUID, @Json(name = "GroupName") var groupName: String,
            @Json(name = "GameType") var gameType: Int, @Json(name = "PlayersInGroup") var playersInGroup: Int,
            @Json(name = "PlayersMax") var playersMax: Int, @Json(name = "VirtualPlayer") var virtualPlayers: Int,
            @Json(name = "Players") var players: ArrayList<UUID>) {

}