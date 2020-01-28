package com.log3900.login

// TODO: Password
data class AuthenticationRequest(
    val username: String
)

data class AuthenticationResponse(
    val status: Int,
    val bearer: String,         // TODO: Replace with correct type
    val sessionToken: String,   // TODO: Replace with correct type
    val error: String
)
