package com.log3900.shared.database

import androidx.room.TypeConverter
import java.util.*

class UUIDConverter {
    @TypeConverter
    fun fromString(id: String): UUID {
        return UUID.fromString(id)
    }

    @TypeConverter
    fun toString(id: UUID): String {
        return id.toString()
    }
}