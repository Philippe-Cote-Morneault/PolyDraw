package com.log3900.profile.stats

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

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
            // TODO: Do call
        }
    }

    private fun onStatsFetchSuccess(userStats: UserStats) {

    }

    private fun onStatsFetchError(error: String?) {

    }
}