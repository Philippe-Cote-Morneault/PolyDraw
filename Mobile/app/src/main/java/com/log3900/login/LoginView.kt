package com.log3900.login

import com.log3900.shared.architecture.ViewNavigator

interface LoginView : ViewNavigator {
    fun setUsernameError(error: String)
    fun setPasswordError(error: String)
    fun showProgresBar()
    fun hideProgressBar()
    fun clearUsernameError()
    fun clearPasswordError()
    fun showErrorDialog(error: String)
}