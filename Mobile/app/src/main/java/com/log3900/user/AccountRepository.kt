package com.log3900.user

import android.content.Context
import com.log3900.MainApplication
import com.log3900.R

class AccountRepository {
    companion object {
        fun getAccount(): Account {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val username    = preferences.getString(context.getString(R.string.preference_file_username_key), "nil")
            val email       = preferences.getString(context.getString(R.string.preference_file_email_key), "nil")
            val firstname   = preferences.getString(context.getString(R.string.preference_file_firstname_key), "nil")
            val lastname    = preferences.getString(context.getString(R.string.preference_file_lastname_key), "nil")
            val sessionToken = preferences.getString(context.getString(R.string.preference_file_session_token_key), "nil")
            val bearerToken = preferences.getString(context.getString(R.string.preference_file_bearer_token_key), "nil")
            
            return Account(
                username!!,
                email!!,
                firstname!!,
                lastname!!,
                sessionToken!!,
                bearerToken!!
            )
        }

        fun createAccount(account: Account) {
            updateAccount(account)
        }

        fun updateAccount(account: Account) {
            val context = MainApplication.instance.applicationContext
            val preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            if (preferences != null) {
                with (preferences.edit()) {
                    putString(context.getString(R.string.preference_file_username_key), account.username)
                    putString(context.getString(R.string.preference_file_email_key), account.email)
                    putString(context.getString(R.string.preference_file_firstname_key), account.firstname)
                    putString(context.getString(R.string.preference_file_lastname_key), account.lastname)
                    putString(context.getString(R.string.preference_file_session_token_key), account.sessionToken)
                    putString(context.getString(R.string.preference_file_bearer_token_key), account.bearerToken)
                    commit()
                }
            }
        }
    }
}
