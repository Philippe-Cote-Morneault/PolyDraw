package com.log3900.game.group

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.log3900.shared.network.rest.Retrofit
import retrofit2.Call
import retrofit2.http.*

interface GroupRestService {
    @GET("groups")
    fun getGroups(@Header("SessionToken") sessionToken: String, @Header("Language") language: String): Call<JsonArray>

    @POST("groups")
    fun createGroup(@Header("SessionToken") sessionToken: String, @Header("Language") language: String,
                    @Body data: JsonObject) : Call<JsonObject>

    @GET("groups/{groupID}")
    fun getGroup(@Header("SessionToken") sessionToken: String, @Header("Language") language: String,
                   @Path("groupID") groupID: String) : Call<JsonObject>

    companion object {
        val service = Retrofit.retrofit.create(GroupRestService::class.java)
    }
}

