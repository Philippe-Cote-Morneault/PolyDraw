package com.log3900.profile

import com.log3900.login.Validator

class ModifyProfilePresenter(var modifyDialog: ModifyProfileDialog) {

    fun validateUsername(username: String): Boolean {
        return if (!Validator.validateUsername(username)) {
            modifyDialog.setUsernameError("Invalid name (must be ${Validator.minUsernameLength}-${Validator.maxUsernameLength} alphanumeric characters)")
            false
        } else {
            modifyDialog.setUsernameError(null)
            true
        }
    }

    fun validatePassword(password: String): Boolean {
        return if (!Validator.validatePassword(password)) {
            modifyDialog.setPasswordError("Invalid password (must be ${Validator.minPasswordLength}-${Validator.maxPasswordLength} characters)")
            false
        } else {
            modifyDialog.setPasswordError(null)
            true
        }
    }

    fun validateEmail(email: String): Boolean {
        return if (!Validator.validateEmail(email)) {
            modifyDialog.setEmailError("Invalid email format")
            false
        } else {
            modifyDialog.setEmailError(null)
            true
        }
    }

    fun validateFirstname(firstname: String): Boolean {
        return if (firstname.isEmpty()) {
            modifyDialog.setFirstnameError("First name cannot be empty")
            false
        } else {
            modifyDialog.setFirstnameError(null)
            true
        }
    }
    fun validateLastname(lastname: String): Boolean {
        return if (lastname.isEmpty()) {
            modifyDialog.setLastnameError("Last name cannot be empty")
            false
        } else {
            modifyDialog.setLastnameError(null)
            true
        }
    }
}