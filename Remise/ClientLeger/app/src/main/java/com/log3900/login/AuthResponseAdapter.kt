package com.log3900.login

import com.squareup.moshi.FromJson

// Unused
class AuthResponseAdapter {

    @FromJson fun parseResponse(response: AuthResponse): AuthResponse {
        when {
            response.bearer != null -> println("Success auth")
            response.error != null -> println("Error auth")
            else -> println("Else res")
        }
        println(response)
        return response
    }
}