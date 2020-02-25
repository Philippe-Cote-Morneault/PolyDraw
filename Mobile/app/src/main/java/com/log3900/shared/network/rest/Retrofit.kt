package com.log3900.shared.network.rest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object Retrofit {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://log3900.fsae.polymtl.ca:5010/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
