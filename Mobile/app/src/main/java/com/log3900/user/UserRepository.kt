package com.log3900.user

import java.util.*

class UserRepository {
    private var userCache: UserCache = UserCache()

    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            if (instance == null) {
                instance = UserRepository()
            }

            return instance!!
        }
    }

    fun getUser(username: String): User {
        if (userCache.containsUser(username)) {
            return userCache.getUser(username)
        } else {
            return User(UUID.randomUUID(), "", "", "", "", 0, 0, 0)
        }
    }
}