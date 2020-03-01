package com.log3900.tutorial

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.log3900.tutorial.slides.WelcomeFragment

class TutorialFragmentPagerAdapter(var fragmentManager: FragmentManager, var slides: ArrayList<Class<*>>) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return slides.size
    }

    override fun getItem(position: Int): TutorialSlideFragment {
        return slides[position].newInstance() as TutorialSlideFragment
    }
}