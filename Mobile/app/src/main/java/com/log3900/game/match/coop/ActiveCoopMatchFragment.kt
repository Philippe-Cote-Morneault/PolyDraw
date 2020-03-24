package com.log3900.game.match.coop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.daimajia.easing.linear.Linear
import com.log3900.R
import com.log3900.game.match.ActiveMatchFragment
import com.log3900.game.match.ffa.ActiveFFAMatchPresenter
import com.log3900.game.match.ffa.ActiveFFAMatchView

class ActiveCoopMatchFragment : ActiveMatchFragment(), ActiveCoopMatchView {
    private var activeCoopMatchPresenter: ActiveCoopMatchPresenter? = null

    // UI
    private lateinit var teamScoreTextView: TextView
    private lateinit var remainingLivesContainer: LinearLayout
    private var remainingLivesHearts: ArrayList<ImageView> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_coop_match, container, false)

        setupUI(rootView)

        activeCoopMatchPresenter = ActiveCoopMatchPresenter(this)
        activeMatchPresenter = activeCoopMatchPresenter

        return rootView
    }

    override fun setupToolbar() {
        super.setupToolbar()

        teamScoreTextView = toolbar.findViewById(R.id.toolbar_active_coop_match_text_view_team_score)
        remainingLivesContainer = toolbar.findViewById(R.id.toolbar_active_coop_match_container_lives)
    }

    override fun setTeamScore(score: String) {
        teamScoreTextView.text = score
    }

    override fun setRemainingLives(count: Int) {
        if (count == remainingLivesHearts.size) {
            return
        }

        if (count < remainingLivesHearts.size) {
            removeRemainingLife()
            return
        }

        remainingLivesContainer.removeAllViews()
        remainingLivesHearts.clear()

        for (i in 0 until count) {
            addRemainingLife()
        }
    }

    override fun addRemainingLife() {
        val newHeart = createHeartImage()
        remainingLivesHearts.add(newHeart)
        remainingLivesContainer.addView(newHeart)
    }

    override fun removeRemainingLife() {
        val viewToRemove = remainingLivesHearts[remainingLivesHearts.size - 1]
        remainingLivesContainer.removeViewAt(remainingLivesHearts.size - 1)
        remainingLivesHearts.remove(viewToRemove)
    }

    override fun onDestroy() {
        activeCoopMatchPresenter = null
        super.onDestroy()
    }

    private fun createHeartImage(): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.ic_heart_red)
        imageView.layoutParams = LinearLayout.LayoutParams(30, 30)

        return imageView
    }
}