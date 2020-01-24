package com.log3900.login

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

interface TestRequestService {
    @GET("hello/")
    fun getHello(): Call<String>
}

object RestClient {
    private const val baseUrl = "http://log3900.fsae.polymtl.ca:5000/"
    private val retrofit = Retrofit.Builder().baseUrl((baseUrl)).build()

    val testRequestService: TestRequestService = retrofit.create(TestRequestService::class.java)
}