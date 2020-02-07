package com.log3900.utils.format.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class TimeStampAdapter {
    @ToJson fun toJson(date: Date): Long {
        return date.time/1000
    }

    @FromJson fun fromJson(date: Long): Date {
        return Date(date * 1000)
    }
}