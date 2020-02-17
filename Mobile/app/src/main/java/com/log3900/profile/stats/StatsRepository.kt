package com.log3900.profile.stats

import com.google.gson.JsonObject
import com.log3900.profile.ProfileRestService
import com.log3900.user.AccountRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.lang.Exception

// Service?
/**
 * Stores all statistics. Getters should be called from a coroutine
 */
object StatsRepository {
    enum class StatsCategory {
        ALL,
        GENERAL,
        CONNECTION,
        MATCHES_PLAYED,
        ACHIEVEMENTS
    }

//    private var subscribers

    private lateinit var userStats: UserStats

    private suspend fun getUserStats(): UserStats {
        if (!StatsRepository::userStats.isInitialized) {
            fetchAllStats()
        }
        return userStats
    }

    suspend fun getAllStats(): UserStats = getUserStats()
    suspend fun getGeneralStats(): GeneralStats = getUserStats().generalStats
    suspend fun getConnectionHistory(): List<Connection> = getUserStats().connectionHistory

    private suspend fun fetchAllStats() {
        userStats = sendStatsRequest()
    }

    private suspend fun sendStatsRequest(): UserStats {
        val userID = "" // TODO: get acutal userID
        val session = AccountRepository.getAccount().sessionToken
        val responseJson = ProfileRestService.service.getStats(session, "EN", userID)   //TODO: get language

        if (responseJson.isSuccessful && responseJson.body() != null) {
            val json = responseJson.body()!!
            return parseJsonToStats(json)
        } else {
            throw Exception("${responseJson.code()} : ${responseJson.errorBody()?.string()}")
        }
    }

    private fun parseJsonToStats(json: JsonObject): UserStats {
        println(json.toString())
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<UserStats> = moshi.adapter(UserStats::class.java)

        return adapter.fromJson(json.toString())!!
    }
}