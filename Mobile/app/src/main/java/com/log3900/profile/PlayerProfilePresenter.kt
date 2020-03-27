package com.log3900.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerProfilePresenter(val profileDialog: PlayerProfileDialogFragment) {
    fun fetchUserInfo() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {

            }
        }
    }
}