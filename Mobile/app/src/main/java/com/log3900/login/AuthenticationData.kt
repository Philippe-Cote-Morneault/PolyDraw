package com.log3900.login

// TODO: Password
data class AuthenticationRequest(
    val username: String
)

data class AuthenticationResponse(
    val bearer: String,
    val sessionToken: String
)
