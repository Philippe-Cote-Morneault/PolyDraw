package com.log3900.shared.architecture

interface ViewNavigator {
    fun navigateTo(target: Class<*>, intentFlags: Int?)
}