package com.log3900.profile.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

//
interface ProfileApi {
    @PUT("/users")
    fun modifyProfileInfo(@Body modifiedUser: ModifiedUser): Call<Unit>
}