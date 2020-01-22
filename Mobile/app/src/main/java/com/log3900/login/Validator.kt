package com.log3900.login

object Validator {
    private const val minLength = 4
    private const val maxLength = 12

    fun validateUsername(username: String) = username.isNotEmpty()
            && username.isAlphanumeric()
            && username.isValidLen()

    fun validatePassword(password: String) = password.isNotEmpty() && password.isValidLen()

    private fun String.isAlphanumeric(): Boolean = this.matches(Regex("[a-zA-Z0-9]+"))
    private fun String.isValidLen(): Boolean = this.length in minLength..maxLength
}