package com.log3900.chat

import com.squareup.moshi.Json
import java.util.*

class Channel(@Json(name = "ID") var ID: UUID, @Json(name = "Name") var name: String, @Json(name = "Users") var users: Array<UUID>)