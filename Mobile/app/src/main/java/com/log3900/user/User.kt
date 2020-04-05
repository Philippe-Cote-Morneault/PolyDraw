package com.log3900.user

import com.squareup.moshi.Json
import java.util.*

class User(@Json(name = "ID") var ID: UUID, @Json(name = "FirstName") var firstName: String, @Json(name = "LastName") var lastName: String,
           @Json(name = "Username") var username: String, @Json(name = "Email") var email: String, @Json(name = "PictureID") var pictureID: Int,
           @Json(name = "CreatedAt") var createdAt: Int, @Json(name = "UpdatedAt") var updatedAt: Int)

class UsernameChanged(
    var userID: UUID,
    var pictureID: Int,
    var newUsername: String,
    var isCPU: Boolean
)