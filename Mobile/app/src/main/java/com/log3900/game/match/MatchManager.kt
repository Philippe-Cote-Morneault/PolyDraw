package com.log3900.game.match

class MatchManager {
    private var matchRepository: MatchRepository

    init {
        matchRepository = MatchRepository.instance!!
    }

    fun getCurrentMatch(): Match {
        return matchRepository.getCurrentMatch()!!
    }

    fun notifyReadyToPlay() {
        matchRepository.notifyReadyToPlay()
    }
}