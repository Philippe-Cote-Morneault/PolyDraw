package com.log3900.tutorial

import com.log3900.tutorial.slides.ChatFragment01
import com.log3900.tutorial.slides.WelcomeFragment

class TutorialManager {
    var tutorialSlides: ArrayList<Class<*>> = arrayListOf(
        WelcomeFragment::class.java,
        ChatFragment01::class.java
    )

}
