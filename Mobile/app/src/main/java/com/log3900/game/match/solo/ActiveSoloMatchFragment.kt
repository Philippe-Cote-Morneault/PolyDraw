package com.log3900.game.match.solo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.log3900.R
import com.log3900.game.match.ActiveMatchFragment

class ActiveSoloMatchFragment : ActiveMatchFragment(), ActiveSoloMatchView {
    private var activeSoloMatchPresenter: ActiveSoloMatchPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_solo_match, container, false)

        setupUI(rootView)

        activeSoloMatchPresenter = ActiveSoloMatchPresenter(this)
        activeMatchPresenter = activeSoloMatchPresenter

        return rootView
    }

    override fun onDestroy() {
        activeSoloMatchPresenter = null
        super.onDestroy()
    }
}