package com.log3900.profile.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ProfileService {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://log3900.fsae.polymtl.ca:5000/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    fun modifyProfile(modifiedUser: ModifiedUser) = retrofit.create(ProfileApi::class.java).modifyProfileInfo(modifiedUser)
}