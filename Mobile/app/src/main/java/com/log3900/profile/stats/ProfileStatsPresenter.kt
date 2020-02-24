package com.log3900.profile.stats

import kotlinx.coroutines.*
import java.lang.Exception

class ProfileStatsPresenter(val statsView: ProfileStatsFragment) {

    fun fetchStats() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    val stats = StatsRepository.getAllUserStats()
                    onStatsFetchSuccess(stats)
                } catch (e: Exception) {
                    onStatsFetchError(e.message)
                }
            }
        }
    }

    private fun onStatsFetchSuccess(userStats: UserStats) {
        println(userStats)
        statsView.showStats(userStats)
    }

    private fun onStatsFetchError(error: String?) {
        println("Error: $error")
    }
}