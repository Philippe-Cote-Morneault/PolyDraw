package com.log3900.game.match.solo

import com.log3900.game.match.MatchManager
import com.log3900.game.match.SoloMatch

class SoloMatchManager : MatchManager() {
    override fun getCurrentMatch(): SoloMatch {
        return matchRepository.getCurrentMatch() as SoloMatch
    }
}