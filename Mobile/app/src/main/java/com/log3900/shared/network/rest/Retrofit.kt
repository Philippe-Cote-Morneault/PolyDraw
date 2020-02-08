package com.log3900.shared.network.rest

import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


object Retrofit {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://log3900.fsae.polymtl.ca:5010/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .add(UUIDAdapter())
            .add(TimeStampAdapter())
            .build()
    }
}
