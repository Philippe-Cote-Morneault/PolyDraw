package com.log3900.user.account

import androidx.room.*
import java.util.*

@Dao
interface AccountDAO {
    @Insert
    fun insertAccount(account: Account)

    @Query("SELECT * FROM account WHERE ID == :id LIMIT 1")
    fun findByID(id: UUID): Account

    @Update
    fun updateAccount(account: Account)

    @Delete
    fun delete(account: Account)
}