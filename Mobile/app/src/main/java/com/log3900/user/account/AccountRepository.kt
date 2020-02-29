package com.log3900.user.account

import android.content.Context
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.shared.database.AppDatabase
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class AccountRepository {
    companion object {
        var currentAccountID: UUID? = null
        private var currentAccount: Account? = null

        fun getAccount(): Single<Account> {
            return Single.create {
                if (currentAccount != null) {
                    it.onSuccess(currentAccount!!)
                } else {
                    it.onSuccess(AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO().findByID(
                        currentAccountID!!))
                }
            }
        }

        fun createAccount(account: Account): Completable {
            return Completable.create {
                AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO()
                    .insertAccount(account)
                it.onComplete()
            }
        }

        fun updateAccount(account: Account): Completable {
            return Completable.create {
                AppDatabase.getInstance(MainApplication.instance.applicationContext).accountDAO()
                    .updateAccount(account)
                it.onComplete()
            }
        }
    }
}
