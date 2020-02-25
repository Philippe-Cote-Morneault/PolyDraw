package com.log3900.shared.ui

interface ProfileView {
    fun setUsernameError(error: String?)
    fun setPasswordError(error: String?)
    fun setEmailError(error: String?)
    fun setFirstnameError(error: String?)
    fun setLastnameError(error: String?)
}