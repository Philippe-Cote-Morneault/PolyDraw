package com.log3900.shared.ui

interface ViewNavigator {
    fun navigateTo(target: Class<*>, intentFlags: Int)
}