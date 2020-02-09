package com.log3900.login

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.log3900.shared.architecture.ViewNavigator

interface LoginView : ViewNavigator {
    fun setUsernameError(error: String)
    fun setPasswordError(error: String)
    fun showProgresBar()
    fun hideProgressBar()
    fun clearUsernameError()
    fun clearPasswordError()
    fun showErrorDialog(title: String, message: String, positiveButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                        negativeButtonClickListener: ((dialog: DialogInterface, which: Int) -> Unit)?)
    fun showProgressDialog(dialog: DialogFragment)
    fun hideProgressDialog(dialog: DialogFragment)
    fun closeView()
}