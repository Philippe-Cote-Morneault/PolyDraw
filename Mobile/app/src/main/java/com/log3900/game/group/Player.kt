package com.log3900.game.group

import com.squareup.moshi.Json
import java.util.*

class Player(@Json(name = "ID") var ID: UUID, @Json(name = "Username") var username: String, @Json(name = "IsCPU") var isCPU: Boolean) {

}