package com.log3900.user.account

import android.content.Context
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.shared.database.AppDatabase
import java.util.*

class AccountRepository {
    companion object {
        var currentAccountID: UUID? = null

        fun getAccount(): Account {
            return AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO().findByID(
                currentAccountID!!)
        }

        fun createAccount(account: Account) {
            AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO().insertAccount(account)
        }

        fun updateAccount(account: Account) {
            AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO().updateAccount(account)
        }
    }
}
