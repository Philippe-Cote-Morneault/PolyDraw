package com.log3900.tutorial

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class TutorialSlideAdapter(var tutorialSlides: ArrayList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tutorial_slide, parent, false)

        return TutorialSlideViewHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TutorialSlideViewHolder).bind(Resources.getSystem().getString(tutorialSlides[position]))
    }

    override fun getItemCount(): Int {
        return tutorialSlides.size
    }
}