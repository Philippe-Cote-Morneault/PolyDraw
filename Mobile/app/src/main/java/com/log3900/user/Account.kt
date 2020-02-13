package com.log3900.user

data class Account(
    var username:       String,
    val email:          String,
    val firstname:      String,
    val lastname:       String,
    var sessionToken:   String,
    var bearerToken:    String
)