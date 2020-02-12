package com.log3900.user

import com.squareup.moshi.Json
import java.util.*

data class OnlineUser(@Json(name = "Name") var name: String, @Json(name = "ID") var ID: UUID)