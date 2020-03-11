package com.log3900.user.account

import android.util.Log
import com.log3900.MainApplication
import com.log3900.shared.database.AppDatabase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.NoSuchElementException

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
            it.onComplete()
        }.subscribeOn(Schedulers.io())
    }

    fun getAccountByID(id: UUID): Single<Account> {
        val single: Single<Account> = Single.create {
            val account = AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO()
                .findByID(id)
            if (account != null) {
                it.onSuccess(account)
            } else {
                it.onError(NoSuchElementException())
            }
        }

        return single.subscribeOn(Schedulers.io())
    }

    fun getAccount(): Account {
        Log.d("SESSION_TOKEN", "Session token: ${currentAccount!!.sessionToken}")
        return currentAccount!!
    }

    fun createAccount(account: Account): Completable {
        currentAccount = account
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

            if (currentAccount?.ID == account.ID) {
                currentAccount = account
            }
        }.subscribeOn(Schedulers.io())
    }
}
