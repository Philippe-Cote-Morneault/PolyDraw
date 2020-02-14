package com.log3900.login.register

import com.log3900.shared.ui.ProfilePresenter

class RegisterPresenter(registerFragment: RegisterFragment) : ProfilePresenter(registerFragment) {
    override val profileView = registerFragment
}