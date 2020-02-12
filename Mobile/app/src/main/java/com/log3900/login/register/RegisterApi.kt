package com.log3900.login.register

import com.log3900.shared.models.UserInfo
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// TODO: Change for proper json model
interface RegisterApi {
    @POST
    fun sendRegisterInfo(@Body info: UserInfo): Call<JSONObject>
}