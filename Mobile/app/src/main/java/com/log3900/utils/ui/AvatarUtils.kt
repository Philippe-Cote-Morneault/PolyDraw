package com.log3900.utils.ui

import com.log3900.R
import com.log3900.user.Account
import com.log3900.user.AccountRepository

/**
 * Returns the ID of the given [Account]'s pictureID linking to the corresponding mipmap
 * @sample imageView.setImageDrawable(ContextCompat.getDrawable(activity, getAccountAvatarID(account)));
 */
fun getAccountAvatarID(account: Account): Int {
    val avatarID = account.pictureID
    return when(avatarID) {
        1 -> R.mipmap.avatar_1
        2 -> R.mipmap.avatar_2
        3 -> R.mipmap.avatar_3
        4 -> R.mipmap.avatar_4
        5 -> R.mipmap.avatar_5
        6 -> R.mipmap.avatar_6
        7 -> R.mipmap.avatar_7
        8 -> R.mipmap.avatar_8
        9 -> R.mipmap.avatar_9
        10 -> R.mipmap.avatar_10
        11 -> R.mipmap.avatar_11
        12 -> R.mipmap.avatar_12
        13 -> R.mipmap.avatar_13
        14 -> R.mipmap.avatar_14
        15 -> R.mipmap.avatar_15
        16 -> R.mipmap.avatar_16

        else -> R.mipmap.avatar_1   // Avatar 1 by default?
    }
}

/**
 * Returns the ID of the current [Account]'s avatar
 */
fun getCurrentAccountAvatarID(): Int {
    val current = AccountRepository.getAccount()
    return getAccountAvatarID(current)
}