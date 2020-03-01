package com.log3900.tutorial

import com.log3900.R
import com.log3900.tutorial.slides.ChatFragment01
import com.log3900.tutorial.slides.WelcomeFragment

object TutorialManager {
    private var activeTutorialTab: Int = 0

    private var slideChangedListeners: ArrayList<((oldPos: Int, newPos: Int) -> Unit)> = arrayListOf()

    var tutorialSlides: HashMap<Int, Class<*>> = hashMapOf(
        R.string.tutorial_slide_welcome_01_title to WelcomeFragment::class.java,
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
}
