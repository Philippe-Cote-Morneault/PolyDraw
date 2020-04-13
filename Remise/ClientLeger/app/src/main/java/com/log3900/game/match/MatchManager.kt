package com.log3900.game.match

import java.util.*
import kotlin.collections.HashMap

abstract class MatchManager {
    protected var matchRepository: MatchRepository

    init {
        matchRepository = MatchRepository.instance!!
    }

    abstract fun getCurrentMatch(): Match

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

    fun requestHint() {
        matchRepository.requestHint()
    }
}