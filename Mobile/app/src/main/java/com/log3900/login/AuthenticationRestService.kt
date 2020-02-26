package com.log3900.login

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.log3900.shared.network.rest.Retrofit
import okhttp3.ResponseBody
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

    @GET("/users/{userID}")
    fun getUserInfo(
        @Header("SessionToken") sessionToken: String,
        @Path("userID") userID: String
    ): Call<JsonObject>

    companion object {
        val service: AuthenticationRestService = Retrofit.retrofit.create(AuthenticationRestService::class.java)
    }
}