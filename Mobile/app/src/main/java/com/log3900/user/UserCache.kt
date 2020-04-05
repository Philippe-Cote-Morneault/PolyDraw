package com.log3900.user

import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

class UserCache {
    private var users: HashMap<UUID, User> = HashMap()

    fun addUser(user: User) {
        if (!users.containsKey(user.ID)) {
            users[user.ID] = user
        }
    }

    fun getUser(userID: UUID): User {
        if (containsUser(userID)) {
            return users[userID]!!
        }

        throw NoSuchElementException("User $userID is not present in cache.")
    }

    fun containsUser(userID: UUID): Boolean {
        return users.containsKey(userID)
    }

    fun clearCache() {
        users.clear()
    }
}