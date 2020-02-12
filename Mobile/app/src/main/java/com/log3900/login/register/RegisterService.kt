package com.log3900.login.register

import com.log3900.shared.models.UserInfo
import com.log3900.shared.network.rest.Retrofit

object RegisterService {
    fun register(info: UserInfo) = Retrofit.retrofit.create(RegisterApi::class.java)
}