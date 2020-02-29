package com.log3900.tutorial

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class TutorialSlideViewHolder : RecyclerView.ViewHolder {
    private var tutorialSlideTitleTextView: TextView
    private lateinit var tutorialSlideTitle: String

    constructor(itemView: View) : super(itemView) {
        tutorialSlideTitleTextView = itemView.findViewById(R.id.list_item_tutorial_slide_title)
    }

    fun bind(slideTitle: String) {
        tutorialSlideTitle = slideTitle
        tutorialSlideTitleTextView.text = tutorialSlideTitle
    }
}