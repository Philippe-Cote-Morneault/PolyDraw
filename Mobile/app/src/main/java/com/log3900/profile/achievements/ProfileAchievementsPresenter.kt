package com.log3900.profile.achievements

import com.log3900.profile.stats.Achievement
import com.log3900.profile.stats.StatsRepository
import com.log3900.profile.stats.UserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class ProfileAchievementsPresenter(val achievementsView: ProfileAchievementsFragment) {

    fun fetchAchievements() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    val achievements = StatsRepository.getAchievements()
                    onAchievementsFetchSucess(achievements)
                } catch (e: Exception) {
                    onAchievementsFetchError(e.message)
                }
            }
        }
    }

    fun onAchievementsFetchSucess(achievements: List<Achievement>) {
        println(achievements)
    }

    fun onAchievementsFetchError(error: String?) {
        println(error)
    }
}