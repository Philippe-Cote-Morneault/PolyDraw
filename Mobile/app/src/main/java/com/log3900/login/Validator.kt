package com.log3900.login

import org.apache.commons.lang3.StringUtils

object Validator {
    const val minUsernameLength = 4
    const val maxUsernameLength = 12
    const val minPasswordLength = 8
    const val maxPasswordLength = 64

    fun validateUsername(username: String) = username.isNotEmpty()
            && username.isAlphanumeric()
            && username.isValidUsernameLen()

    fun validatePassword(password: String) = password.isNotEmpty() && password.isValidPasswordLen()

    private fun String.isAlphanumeric(): Boolean = StringUtils.isAlphanumeric(this)
    private fun String.isValidUsernameLen(): Boolean = this.length in minUsernameLength..maxUsernameLength
    private fun String.isValidPasswordLen(): Boolean = this.length in minPasswordLength..maxPasswordLength
}