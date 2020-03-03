package com.log3900.game.group

import com.google.gson.JsonObject

class GroupCreated(var groupName: String?, var playersMax: Int, var virtualPlayers: Int, var gameType: Int, var difficulty: Int?) {

    fun toJsonObject(): JsonObject {
        val groupCreated = JsonObject().apply {
            addProperty("PlayersMax", playersMax)
            addProperty("VirtualPlayers", virtualPlayers)
            addProperty("GameType", gameType)
        }

        if (groupName != null) {
            groupCreated.addProperty("GroupName", groupName)
        }

        if (difficulty != null) {
            groupCreated.addProperty("Difficulty", difficulty)
        }

        return groupCreated
    }
}