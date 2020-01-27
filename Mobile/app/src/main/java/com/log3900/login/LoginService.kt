package com.log3900.login

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface TestRequestService {
    @GET("api/gibberish/")
    fun getHello(): Call<ResponseBody>
}

object RestClient {
    // TODO: Replace url with:
//    private const val baseUrl = "http://log3900.fsae.polymtl.ca:5000/"
    private const val baseUrl = "https://www.randomtext.me/api/gibberish/h1/5-15/"
    private val httpClient = OkHttpClient().newBuilder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .build()

    val testRequestService: TestRequestService = retrofit.create(TestRequestService::class.java)
}