package com.log3900.game.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.log3900.R
import com.log3900.game.match.UI.WordGuessingView

class ActiveMatchFragment : Fragment(), ActiveMatchView {
    private var activeMatchPresenter: ActiveMatchPresenter? = null

    // UI
    private lateinit var guessingView: WordGuessingView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_match, container, false)
        guessingView = rootView.findViewById(R.id.fragment_active_match_guess_container)
        guessingView.setWordLength(5)

        activeMatchPresenter = ActiveMatchPresenter(this)

        return rootView
    }


    override fun onDestroy() {
        activeMatchPresenter?.destroy()
        activeMatchPresenter = null
        super.onDestroy()
    }
}