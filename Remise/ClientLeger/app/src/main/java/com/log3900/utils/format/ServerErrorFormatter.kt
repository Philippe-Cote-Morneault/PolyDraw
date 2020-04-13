package com.log3900.utils.format

object ServerErrorFormatter {
    fun format(error: String): String {
        return error
            .removePrefix("{\"Error\":\"")
            .removeSuffix("\"}")
    }
}