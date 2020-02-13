package com.log3900.profile

import com.log3900.shared.network.rest.Retrofit
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ProfileRestService {

    @POST("users/")
    fun modifyProfile(
        @Header("SessionToken") sessionToken: String,
        @Header("Language")     language: String,
        @Body data: JSONObject
    ): Call<JSONObject>

    companion object {
        val service = Retrofit.retrofit.create(ProfileRestService::class.java)
    }
}