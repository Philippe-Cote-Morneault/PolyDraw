package com.log3900.game.group

import com.google.gson.JsonObject

class GroupCreated(var groupName: String?, var playersMax: Int, var virtualPlayers: Int, var gameType: MatchMode, var difficulty: Difficulty?) {

    fun toJsonObject(): JsonObject {
        val groupCreated = JsonObject().apply {
            addProperty("PlayersMax", playersMax)
            addProperty("VirtualPlayers", virtualPlayers)
            addProperty("GameType", gameType.ordinal)
        }

        if (groupName != null) {
            groupCreated.addProperty("GroupName", groupName)
        }

        if (difficulty != null) {
            groupCreated.addProperty("Difficulty", difficulty?.ordinal)
        }

        return groupCreated
    }
}