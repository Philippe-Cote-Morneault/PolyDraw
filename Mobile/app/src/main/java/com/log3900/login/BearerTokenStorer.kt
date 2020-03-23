package com.log3900.login

import android.content.Context
import android.content.SharedPreferences
import com.log3900.MainApplication
import com.log3900.R

object BearerTokenStorer {
    private val sharedPrefs: SharedPreferences = with (MainApplication.instance) {
        getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }
    private val bearerPrefKey = MainApplication.instance.resources.getString(R.string.preference_file_bearer_token_key)


    fun getBearer(): String? = sharedPrefs.getString(bearerPrefKey, null)

    fun storeBearer(token: String) {
        with(sharedPrefs.edit()) {
            putString(bearerPrefKey, token)
            commit()
        }
    }

    fun resetBearer() {
        with(sharedPrefs.edit()) {
            putString(bearerPrefKey, null)
            commit()
        }
    }
}