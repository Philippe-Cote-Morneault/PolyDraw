package com.log3900.login

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationService {
    @POST("/auth")
    fun authenticate(@Body data: AuthenticationRequest): Call<ResponseBody>
}
