package com.log3900.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class TutorialSlidesListFragment : Fragment() {
    private lateinit var tutorialSlidesRecyclerView: RecyclerView
    private lateinit var tutorialSlidesAdapter: TutorialSlideAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_tutorial_slides_list, container, false)

        tutorialSlidesRecyclerView = root.findViewById(R.id.fragment_tutorial_slides_list_recycler_view)

        setupRecyclerView()

        TutorialManager.addSlideChangedListener { oldPos, newPos ->
            onSlideChanged(oldPos, newPos)
        }

        return root
    }

    private fun onSlideChanged(oldPos: Int, newPos: Int) {
        tutorialSlidesAdapter.notifyItemChanged(oldPos)
        tutorialSlidesAdapter.notifyItemChanged(newPos)
    }

    private fun setupRecyclerView() {
        tutorialSlidesAdapter = TutorialSlideAdapter(TutorialManager.tutorialSlideTitles)
        tutorialSlidesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = tutorialSlidesAdapter
        }
    }
}