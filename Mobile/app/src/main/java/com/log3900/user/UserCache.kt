package com.log3900.user

class UserCache {
    private var users: HashMap<String, User> = HashMap()

    fun addUser(user: User) {
        if (!users.containsKey(user.username)) {
            users[user.username] = user
        }
    }
}