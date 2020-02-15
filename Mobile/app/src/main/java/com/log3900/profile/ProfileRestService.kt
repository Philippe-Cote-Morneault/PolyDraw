package com.log3900.profile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.shared.network.rest.Retrofit
import retrofit2.Call
import retrofit2.http.*

interface ProfileRestService {

    // Profile modification
    @PUT("users/")
    fun modifyProfile(
        @Header("SessionToken") sessionToken: String, @Header("Language") language: String,
        @Body data: JsonObject
    ): Call<JsonArray>

    // Statistics
    @GET("stats/{userid}")
    fun getStats(
        @Header("SessionToken") sessionToken: String, @Header("Language") language: String,
        @Path("userid") userId: String
    ): Call<JsonObject>

    companion object {
        val service = Retrofit.retrofit.create(ProfileRestService::class.java)
    }
}