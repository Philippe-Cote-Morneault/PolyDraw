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
            
            return User(username!!)
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
                    commit()
                }
            }
        }
    }
}
