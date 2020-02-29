package com.log3900.tutorial.slides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.log3900.R
import com.log3900.tutorial.TutorialSlideFragment

class ChatFragment01 : TutorialSlideFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tutorial_slide_chat_01, container, false)
        return root
    }
}