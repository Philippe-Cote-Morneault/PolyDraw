package com.log3900.tutorial

import com.log3900.R
import com.log3900.tutorial.slides.*
import com.log3900.user.account.AccountRepository
import io.reactivex.Completable

object TutorialManager {
    private var activeTutorialTab: Int = 0

    private var slideChangedListeners: ArrayList<((oldPos: Int, newPos: Int) -> Unit)> = arrayListOf()

    var tutorialSlides: HashMap<Int, Class<*>> = hashMapOf(
        R.string.tutorial_slide_welcome_01_title to WelcomeFragment::class.java,
        R.string.tutorial_slide_home_view_title to HomeViewFragment::class.java,
        R.string.tutorial_slide_active_match_channel_title to ActiveMatchChannelFragment::class.java,
        R.string.tutorial_slide_virtual_players_title to VirtualPlayersFragment::class.java,
        R.string.tutorial_ffa_match_title to FFAMatchFragment::class.java,
        R.string.tutorial_slide_solo_match_title to SoloMatchFragment::class.java,
        R.string.tutorial_slide_coop_match_title to CoopMatchFragment::class.java,
        R.string.tutorial_slide_chat_01_title to ChatFragment01::class.java
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

    fun getSlidesAsArrayList(): ArrayList<Class<*>> {
        var arrayList = arrayListOf<Class<*>>()

        tutorialSlides.forEach {
            arrayList.add(it.value)
        }

        return arrayList
    }

    fun getSlidesTitlesAsArrayList(): ArrayList<Int> {
        val arrayList = arrayListOf<Int>()

        tutorialSlides.forEach {
            arrayList.add(it.key)
        }

        return arrayList
    }

    fun setAccountFinishedTutorial(): Completable {
        val account = AccountRepository.getInstance().getAccount()
        account.tutorialDone = true
        return AccountRepository.getInstance().updateAccount(account)
    }
}
