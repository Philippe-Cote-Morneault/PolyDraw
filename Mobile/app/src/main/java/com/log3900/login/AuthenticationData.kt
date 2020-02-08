package com.log3900.login

import com.squareup.moshi.Json

// TODO: Password
data class AuthenticationRequest(
    @Json(name = "Username") val username: String
)

data class AuthResponse(
    @Json(name = "Bearer") val bearer: String?,
    @Json(name = "SessionToken") val sessionToken: String?,
    @Json(name = "Error") val error: String?
)

data class SuccessAuthResponse(
    val bearer: String,
    val sessionToken: String
)

data class ErrorAuthResponse(
    val error: String
)
