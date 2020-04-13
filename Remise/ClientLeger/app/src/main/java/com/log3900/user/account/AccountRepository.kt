package com.log3900.user.account

import android.content.Context
import android.util.Log
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.shared.database.AppDatabase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class AccountRepository {
    private var currentAccount: Account? = null

    companion object {
        private var instance: AccountRepository? = null

        fun getInstance(): AccountRepository {
            if (instance == null) {
                instance = AccountRepository()
            }

            return instance!!
        }

    }

    fun setCurrentAccount(accountID: UUID): Completable {
        return Completable.create {
            currentAccount =
                AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO()
                    .findByID(
                        accountID
                    )
        }.subscribeOn(Schedulers.io())
    }

    fun getAccount(): Account {
        return currentAccount!!
    }

    fun createAccount(account: Account): Completable {
        return Completable.create {
            AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO()
                .insertAccount(account)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
    }

    fun updateAccount(account: Account): Completable {
        return Completable.create {
            AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO()
                .updateAccount(account)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
    }
}
