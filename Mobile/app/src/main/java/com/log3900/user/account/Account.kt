package com.log3900.user.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Account(
    @PrimaryKey var ID: UUID,
    var username:       String,
    val pictureID:      Int,
    val email:          String,
    val firstname:      String,
    val lastname:       String,
    var sessionToken:   String,
    var bearerToken:    String
)