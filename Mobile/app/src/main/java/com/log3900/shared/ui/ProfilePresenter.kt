package com.log3900.shared.ui

import com.log3900.login.Validator

open class ProfilePresenter(open val profileView: ProfileView) {
    fun validateUsername(username: String): Boolean {
        return if (!Validator.validateUsername(username)) {
            profileView.setUsernameError("Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)")
            false
        } else {
            profileView.setUsernameError(null)
            true
        }
    }

    fun validatePassword(password: String): Boolean {
        return if (!Validator.validatePassword(password)) {
            profileView.setPasswordError("Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)")
            false
        } else {
            profileView.setPasswordError(null)
            true
        }
    }

    fun validateEmail(email: String): Boolean {
        return if (!Validator.validateEmail(email)) {
            profileView.setEmailError("Invalid email format")
            false
        } else {
            profileView.setEmailError(null)
            true
        }
    }

    fun validateFirstname(firstname: String): Boolean {
        return if (firstname.isEmpty()) {
            profileView.setFirstnameError("First name cannot be empty")
            false
        } else {
            profileView.setFirstnameError(null)
            true
        }
    }
    fun validateLastname(lastname: String): Boolean {
        return if (lastname.isEmpty()) {
            profileView.setLastnameError("Last name cannot be empty")
            false
        } else {
            profileView.setLastnameError(null)
            true
        }
    }

}