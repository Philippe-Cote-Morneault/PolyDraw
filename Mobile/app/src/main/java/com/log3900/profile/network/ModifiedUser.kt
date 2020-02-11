package com.log3900.profile.network

// TODO: Update with avatar + error?
data class ModifiedUser(
    val username:   String,
    val firstName:  String,
    val lastName:   String,
    val password:   String,
    val email:      String
)