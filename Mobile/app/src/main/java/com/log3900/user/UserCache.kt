package com.log3900.user

class UserCache {
    private var users: HashMap<String, User> = HashMap()

    fun addUser(user: User) {
        if (!users.containsKey(user.username)) {
            users[user.username] = user
        }
    }

    fun getUser(username: String): User {
        if (containsUser(username)) {
            return users[username]!!
        }

        throw NoSuchElementException("User $username is not present in cache.")
    }

    fun containsUser(username: String): Boolean {
        return users.containsKey(username)
    }
}