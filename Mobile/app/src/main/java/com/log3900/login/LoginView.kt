package com.log3900.login

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.log3900.shared.architecture.ViewNavigator

enum class LoginErrorType {
    GET_ACCOUNT_INFO,
    CONNECTION_REFUSED,
    CONNECTION_TIMEOUT,
    SOCKET_CONNECTION_TIMEOUT,
    AUTH_ERROR
}

interface LoginView : ViewNavigator {
    fun setUsernameError(error: String)
    fun setPasswordError(error: String)
    fun showProgresBar()
    fun hideProgressBar()
    fun clearUsernameError()
    fun clearPasswordError()
    fun showErrorDialog(title: String, message: String, errorType: LoginErrorType?, positiveButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                        negativeButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?)
    fun showProgressDialog(dialog: DialogFragment)
    fun hideProgressDialog(dialog: DialogFragment)
    fun showWelcomeBackMessage(username: String)
    fun enableView()
    fun disableView()
    fun closeView()
}