package com.log3900.user

import android.content.Context
import com.log3900.MainApplication
import com.log3900.R

class UserRepository {
    companion object {
        fun getUser(): User {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val username = preferences.getString(context.getString(R.string.preference_file_username_key), "nil")
            val sessionToken = preferences.getString(context.getString(R.string.preference_file_session_token_key), "nil")
            val bearerToken = preferences.getString(context.getString(R.string.preference_file_bearer_token_key), "nil")
            
            return User(username!!, sessionToken!!, bearerToken!!)
        }

        fun createUser(user: User) {
            updateUser(user)
        }

        fun updateUser(user: User) {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            if (preferences != null) {
                with (preferences.edit()) {
                    putString(context.getString(R.string.preference_file_username_key), user.username)
                    putString(context.getString(R.string.preference_file_session_token_key), user.sessionToken)
                    putString(context.getString(R.string.preference_file_bearer_token_key), user.bearerToken)
                    commit()
                }
            }
        }
    }
}
