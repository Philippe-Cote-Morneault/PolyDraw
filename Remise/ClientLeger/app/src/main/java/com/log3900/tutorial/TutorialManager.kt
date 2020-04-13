package com.log3900.tutorial

import com.log3900.R
import com.log3900.tutorial.slides.*
import com.log3900.user.account.AccountRepository
import io.reactivex.Completable

object TutorialManager {
    private var activeTutorialTab: Int = 0

    private var slideChangedListeners: ArrayList<((oldPos: Int, newPos: Int) -> Unit)> = arrayListOf()
    var tutorialSlides = arrayListOf<Class<*>>(
        WelcomeFragment::class.java,
        HomeViewFragment::class.java,
        ActiveMatchChannelFragment::class.java,
        VirtualPlayersFragment::class.java,
        FFAMatchFragment::class.java,
        SoloMatchFragment::class.java,
        CoopMatchFragment::class.java
    )

    var tutorialSlideTitles = arrayListOf<Int>(
        R.string.tutorial_slide_welcome_title,
        R.string.tutorial_slide_home_view_title,
        R.string.tutorial_slide_active_match_channel_title,
        R.string.tutorial_slide_virtual_players_title,
        R.string.tutorial_ffa_match_title,
        R.string.tutorial_slide_solo_match_title,
        R.string.tutorial_slide_coop_match_title
    )

    fun addSlideChangedListener(listener: ((oldPos: Int, newPos: Int) -> Unit)) {
        slideChangedListeners.add(listener)
    }

    fun changeActiveTutorialSlide(position: Int) {
        slideChangedListeners.forEach {
            it(activeTutorialTab, position)
        }

        activeTutorialTab = position
    }

    fun getActiveTutorialSlide(): Int {
        return activeTutorialTab
    }

    fun setAccountFinishedTutorial(): Completable {
        val account = AccountRepository.getInstance().getAccount()
        account.tutorialDone = true
        return AccountRepository.getInstance().updateAccount(account)
    }
}
