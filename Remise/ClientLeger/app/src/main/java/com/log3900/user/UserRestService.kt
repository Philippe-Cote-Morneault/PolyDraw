package com.log3900.user

import com.google.gson.JsonObject
import com.log3900.shared.network.rest.Retrofit
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserRestService {
    @GET("users/{ID}")
    fun getUser(@Header("SessionToken") sessionToken: String, @Header("Language") language: String, @Path("ID") userID: String): Call<JsonObject>

    companion object {
        val service = Retrofit.retrofit.create(UserRestService::class.java)
    }

}