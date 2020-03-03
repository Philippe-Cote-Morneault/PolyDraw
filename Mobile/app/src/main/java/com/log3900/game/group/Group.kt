package com.log3900.game.group

import com.squareup.moshi.Json
import java.util.*
import kotlin.collections.ArrayList

class Group(@Json(name = "ID") var ID: UUID, @Json(name = "GroupName") var groupName: String,
            @Json(name = "PlayersMax") var playersMax: Int, @Json(name = "VirtualPlayer") var virtualPlayers: Int,
            @Json(name = "GameType") var gameType: Int, @Json(name = "Difficulty") var difficulty: Int,
            @Json(name = "Status") var status: Int, @Json(name = "Owner") var owner: UUID,
            @Json(name = "Players") var players: ArrayList<UUID>) {

}