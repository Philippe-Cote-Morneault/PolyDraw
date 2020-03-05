package com.log3900.game.group

import com.log3900.R
import com.squareup.moshi.Json
import java.util.*
import kotlin.collections.ArrayList

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;

    companion object {
        fun stringRes(item: Difficulty): Int {
            when (item) {
                EASY -> return R.string.difficulty_easy_title
                MEDIUM -> return R.string.difficulty_medium_title
                HARD -> return R.string.difficulty_hard_title
            }
        }
    }
}

enum class MatchMode {
    FFA,
    SOLO,
    COOP;

    companion object {
        fun stringRes(item: MatchMode): Int {
            when (item) {
                FFA -> return R.string.match_mode_ffa_title
                COOP -> return R.string.match_mode_coop_title
                SOLO -> return R.string.match_mode_solo_title
            }
        }
    }
}

class Group(@Json(name = "ID") var ID: UUID, @Json(name = "GroupName") var groupName: String,
            @Json(name = "PlayersMax") var playersMax: Int,
            @Json(name = "GameType") var gameType: MatchMode, @Json(name = "Difficulty") var difficulty: Difficulty,
            @Json(name = "OwnerID") var ownerID: UUID, @Json(name = "OwnerName") var ownerName: String,
            @Json(name = "Players") var players: ArrayList<Player>) {

}