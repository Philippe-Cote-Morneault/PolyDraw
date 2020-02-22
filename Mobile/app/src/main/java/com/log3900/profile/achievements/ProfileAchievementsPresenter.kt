package com.log3900.profile.achievements

import com.log3900.profile.stats.Achievement
import com.log3900.profile.stats.StatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class ProfileAchievementsPresenter(private val achievementsView: ProfileAchievementsFragment) {

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
        achievementsView.updateAchievementsCount(achievements.size)
    }

    fun onAchievementsFetchError(error: String?) {
        // TODO: Error handling
        println(error)
    }
}