package com.log3900.login

import com.google.gson.JsonObject
import com.log3900.shared.network.rest.Retrofit
import retrofit2.Call
import retrofit2.http.*

interface AuthenticationRestService {
    @POST("/auth")
    fun authenticate(@Body data: JsonObject): Call<JsonObject>

    @POST("/auth/register")
    fun register(
        @Header("Language") language: String,
        @Body data: JsonObject
    ): Call<JsonObject>

    @GET("/users/{ID}")
    fun getUserInfo(
        @Header("SessionToken") sessionToken: String,
        @Path("ID") userID: String
    ): Call<JsonObject>

    companion object {
        val service: AuthenticationRestService = Retrofit.retrofit.create(AuthenticationRestService::class.java)
    }
}
