package com.log3900.shared.network.rest

import com.log3900.utils.format.moshi.TimeStampAdapter
import com.log3900.utils.format.moshi.UUIDAdapter
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Retrofit {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://log3900.fsae.polymtl.ca:5000/")
        .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
        .build()

    fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .add(UUIDAdapter())
            .add(TimeStampAdapter())
            .build()
    }
}
