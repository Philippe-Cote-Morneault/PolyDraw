package com.log3900.user

data class Account(
    var username:       String,
    val firstname:      String,
    val lastname:       String,
    val email:          String,
    var sessionToken:   String,
    var bearerToken:    String
)