package com.log3900.user

import java.util.*

data class Account(
    var userID:         UUID,
    var username:       String,
    val pictureID:      Int,
    val email:          String,
    val firstname:      String,
    val lastname:       String,
    var sessionToken:   String,
    var bearerToken:    String
)