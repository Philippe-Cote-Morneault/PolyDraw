package com.log3900.tutorial

import com.log3900.R
import com.log3900.tutorial.slides.ChatFragment01
import com.log3900.tutorial.slides.WelcomeFragment

class TutorialManager {
    var tutorialSlides: HashMap<Int, Class<*>> = hashMapOf(
        R.string.tutorial_slide_welcome_01_title to WelcomeFragment::class.java,
        R.string.tutorial_slide_chat_01_title to ChatFragment01::class.java
    )

    fun getSlidesAsArrayList(): ArrayList<Class<*>> {
        var arrayList = arrayListOf<Class<*>>()

        tutorialSlides.forEach {
            arrayList.add(it.value)
        }

        return arrayList
    }

}
