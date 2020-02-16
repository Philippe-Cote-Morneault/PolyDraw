package com.log3900.profile.stats

import com.google.gson.JsonObject
import com.log3900.profile.ProfileRestService
import com.log3900.user.AccountRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileStatsPresenter(val statsView: ProfileStatsFragment) {

    fun fetchStats() {
        sendStatsRequest()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userStats ->
                onStatsFetchSuccess(userStats)
            }, { error ->
                onStatsFetchError(error.message)
            })
    }

    private fun sendStatsRequest(): Single<UserStats> {
        return Single.create {
            val userID = "" // TODO: get acutal userID
            val session = AccountRepository.getAccount().sessionToken
            val call = ProfileRestService.service.getStats(session, "EN", userID)   //TODO: get language
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful && response.body() != null) {
                        val json = response.body()!!
                        it.onSuccess(parseJsonToStats(json))
                    } else {
                        it.onError(Throwable(response.errorBody().toString()))
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }

    private fun onStatsFetchSuccess(userStats: UserStats) {
        println(userStats)
        println(userStats.achievements)
        statsView.showStats(userStats.generalStats)
    }

    private fun onStatsFetchError(error: String?) {

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