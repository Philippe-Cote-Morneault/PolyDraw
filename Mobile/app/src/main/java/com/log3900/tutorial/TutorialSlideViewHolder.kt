package com.log3900.tutorial

import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.shared.ui.ThemeUtils

class TutorialSlideViewHolder : RecyclerView.ViewHolder {
    private var rootView: LinearLayout
    private var tutorialSlideTitleTextView: TextView
    private lateinit var tutorialSlideTitle: String
    private var slideIndex: Int = 0

    constructor(itemView: View, clickListener: ClickListener) : super(itemView) {
        rootView = itemView.findViewById(R.id.list_item_tutorial_slide_view_holder)
        tutorialSlideTitleTextView = itemView.findViewById(R.id.list_item_tutorial_slide_title)

        rootView.setOnClickListener {
            clickListener.onSlideClick(slideIndex)
        }
    }

    fun bind(slideTitle: String, slideIndex: Int, isActiveSlide: Boolean) {
        this.slideIndex = slideIndex
        tutorialSlideTitle = slideTitle
        tutorialSlideTitleTextView.text = tutorialSlideTitle

        if (isActiveSlide) {
            rootView.setBackgroundColor(ThemeUtils.resolveAttribute(R.attr.colorPrimaryLight))
            tutorialSlideTitleTextView.setTextColor(ThemeUtils.resolveAttribute(R.attr.colorOnPrimaryLight))
        } else {
            rootView.setBackgroundColor(Color.parseColor("#F5F5F5"))
            tutorialSlideTitleTextView.setTextColor(Color.parseColor("#000000"))
        }
    }

    interface ClickListener {
        fun onSlideClick(slideIndex: Int)
    }
}
