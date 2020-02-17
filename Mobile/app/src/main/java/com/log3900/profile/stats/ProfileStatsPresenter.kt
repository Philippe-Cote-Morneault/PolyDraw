package com.log3900.profile.stats

import com.google.gson.JsonObject
import com.log3900.profile.ProfileRestService
import com.log3900.user.AccountRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ProfileStatsPresenter(val statsView: ProfileStatsFragment) {

    fun fetchStats() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    val stats = StatsRepository.getAllStats()
                    onStatsFetchSuccess(stats)
                } catch (e: Exception) {
                    onStatsFetchError(e.message)
                }
            }
        }
    }

    private fun onStatsFetchSuccess(userStats: UserStats) {
        println(userStats)
        println(userStats.achievements)
        statsView.showStats(userStats.generalStats)
    }

    private fun onStatsFetchError(error: String?) {
        println("Error: $error")
    }
}