package com.log3900.shared.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.log3900.user.account.Account
import com.log3900.user.account.AccountDAO

@Database(entities = [Account::class], version = 1)
@TypeConverters(UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDAO(): AccountDAO

    companion object {
        var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "AppDatabase").build()
            }

            return instance!!
        }

        fun destroy() {
            instance = null
        }
    }
}