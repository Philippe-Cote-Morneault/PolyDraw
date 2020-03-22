package com.log3900.settings.sound

import android.content.Context
import android.media.MediaPlayer
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.user.account.AccountRepository
import io.reactivex.Completable

class SoundManager {
    companion object {
        private var soundEffectsMediaPlayer: MediaPlayer? = null
        private var musicMediaPlayer: MediaPlayer? = null

        fun playSoundEffect(context: Context, audioRes: Int) {
            if (AccountRepository.getInstance().getAccount().soundEffectsOn) {
                if (soundEffectsMediaPlayer != null && soundEffectsMediaPlayer!!.isPlaying) {
                    return
                }
                soundEffectsMediaPlayer = MediaPlayer.create(context, audioRes)
                soundEffectsMediaPlayer?.start()
            }
        }

        fun playMusic(context: Context, audioRes: Int) {
            if (AccountRepository.getInstance().getAccount().musicOn) {
                if (musicMediaPlayer != null && musicMediaPlayer!!.isPlaying) {
                    return
                }
                musicMediaPlayer = MediaPlayer.create(context, audioRes)
                musicMediaPlayer?.start()
            }
        }

        fun toggleSoundEffect(enabled: Boolean): Completable {
            val account = AccountRepository.getInstance().getAccount()
            account.soundEffectsOn = enabled
            return AccountRepository.getInstance().updateAccount(account)
        }

        fun toggleMusic(enabled: Boolean): Completable {
            val account = AccountRepository.getInstance().getAccount()
            account.musicOn = enabled
            return AccountRepository.getInstance().updateAccount(account)
        }

        fun areSoundEffectsEnabled(): Boolean {
            return AccountRepository.getInstance().getAccount().soundEffectsOn
        }

        fun isMusicEnabled(): Boolean {
            return AccountRepository.getInstance().getAccount().musicOn
        }
    }
}