package com.log3900.game.match.ffa

import com.log3900.game.match.FFAMatch
import com.log3900.game.match.Match
import com.log3900.game.match.MatchManager

class FFAMatchManager : MatchManager() {

    override fun getCurrentMatch(): FFAMatch {
        return matchRepository.getCurrentMatch() as FFAMatch
    }
}