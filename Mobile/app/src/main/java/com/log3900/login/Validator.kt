package com.log3900.login

object Validator {
    const val minUsernameLength = 4
    const val maxUsernameLength = 12
    const val minPasswordLength = 8
    const val maxPasswordLength = 64

    fun validateUsername(username: String) = username.isNotEmpty()
            && username.isAlphanumeric()
            && username.isValidUsernameLen()

    fun validatePassword(password: String) = password.isNotEmpty() && password.isValidPasswordLen()

    private fun String.isAlphanumeric(): Boolean = this.matches("^[a-z0-9_]{4,12}\$".toRegex())
    private fun String.isValidUsernameLen(): Boolean = this.length in minUsernameLength..maxUsernameLength
    private fun String.isValidPasswordLen(): Boolean = this.length in minPasswordLength..maxPasswordLength

    fun validateEmail(email: String) = email.isNotEmpty() && email.matches(
        Regex("^[\\w!#\$%&'*+\\-/=?\\^_`{|}~]+(\\.[\\w!#\$%&'*+\\-/=?\\^_`{|}~]+)*@((([\\-\\w]+\\.)+[a-zA-Z]{2,4})|(([0-9]{1,3}\\.){3}[0-9]{1,3}))\\z")
    )
}