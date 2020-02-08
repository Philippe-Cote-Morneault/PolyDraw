package com.log3900.login

import com.log3900.shared.network.rest.Retrofit
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationRestService {
    @POST("/auth")
    fun authenticate(@Body data: AuthenticationRequest): Call<AuthResponse>

    companion object {
        val service: AuthenticationRestService = Retrofit.retrofit.create(AuthenticationRestService::class.java)
    }

}
