package com.log3900.profile.stats

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        Log.d("STATS_PROFILE", "Error: $error")
        println("Error: $error")
    }
}