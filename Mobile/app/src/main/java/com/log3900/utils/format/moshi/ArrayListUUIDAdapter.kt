package com.log3900.utils.format.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*
import kotlin.collections.ArrayList

class ArrayListUUIDAdapter {
    @ToJson
    fun toJson(arrayList: ArrayList<UUID>): List<UUID> {
        return arrayList.toList()
    }

    @FromJson
    fun fromJson(list: List<UUID>): ArrayList<UUID> {
        return ArrayList(list)
    }
}