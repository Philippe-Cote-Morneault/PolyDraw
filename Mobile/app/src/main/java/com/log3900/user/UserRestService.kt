package com.log3900.user

import com.google.gson.JsonObject
import com.log3900.shared.network.rest.Retrofit
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatRestService {
    @GET("users/{userID}")
    fun getChannel(@Path("userID") userID: String): Call<JsonObject>

    companion object {
        val service = Retrofit.retrofit.create(ChatRestService::class.java)
    }

}