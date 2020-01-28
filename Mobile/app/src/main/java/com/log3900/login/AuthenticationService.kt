package com.log3900.login

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

interface AuthenticationService {
    @POST("/auth")
    fun authenticate(@Body data: AuthenticationRequest): Call<AuthenticationResponse>
}
