package com.log3900.game.match

import java.util.*
import kotlin.collections.HashMap

class MatchManager {
    private var matchRepository: MatchRepository

    init {
        matchRepository = MatchRepository.instance!!
    }

    fun getCurrentMatch(): Match {
        return matchRepository.getCurrentMatch()!!
    }

    fun getPlayerScores(): HashMap<UUID, Int> {
        return matchRepository.getPlayerScores()
    }

    fun notifyReadyToPlay() {
        matchRepository.notifyReadyToPlay()
    }

    fun makeGuess(text: String) {
        matchRepository.makeGuess(text)
    }

    fun leaveMatch() {
        matchRepository.leaveMatch()
    }
}