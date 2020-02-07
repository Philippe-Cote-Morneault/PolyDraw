package com.log3900.utils.format.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class UUIDAdapter {
    @ToJson
    fun toJson(uuid: UUID): String {
        return uuid.toString()
    }

    @FromJson
    fun fromJson(uuid: String): UUID {
        return UUID.fromString(uuid)
    }
}