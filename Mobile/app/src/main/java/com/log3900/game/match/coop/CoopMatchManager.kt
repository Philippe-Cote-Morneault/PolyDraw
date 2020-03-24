package com.log3900.game.match.coop

import com.log3900.game.match.CoopMatch
import com.log3900.game.match.Match
import com.log3900.game.match.MatchManager

class CoopMatchManager : MatchManager() {
    override fun getCurrentMatch(): CoopMatch {
        return matchRepository.getCurrentMatch() as CoopMatch
    }
}