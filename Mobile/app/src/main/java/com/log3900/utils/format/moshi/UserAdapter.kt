package com.log3900.utils.format.moshi

import com.google.gson.JsonObject
import com.log3900.user.UsernameChanged
import java.util.*

object UserAdapter {
    fun jsonToUsernameChanged(jsonObject: JsonObject): UsernameChanged {
        val userID = UUID.fromString(jsonObject.get("UserID").asString)
        val pictureID = jsonObject.get("PictureID").asInt
        val newUsername = jsonObject.get("NewName").asString
        val isCPU = jsonObject.get("IsCPU").asBoolean

        return UsernameChanged(userID, pictureID, newUsername, isCPU)
    }
}