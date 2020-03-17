package com.log3900.settings.sound

import android.content.Context
import android.media.MediaPlayer
import com.log3900.R
import com.log3900.user.account.AccountRepository

class SoundManager {
    companion object {
        fun playSoundEffect(context: Context, audioRes: Int) {
            if (AccountRepository.getInstance().getAccount().soundEffectsOn) {
                val musicPlayer = MediaPlayer.create(context, audioRes)
                musicPlayer.start()
            }
        }

        fun playMusic(context: Context, audioRes: Int) {
            if (AccountRepository.getInstance().getAccount().musicOn) {
                val musicPlayer = MediaPlayer.create(context, audioRes)
                musicPlayer.start()
            }
        }
    }
}