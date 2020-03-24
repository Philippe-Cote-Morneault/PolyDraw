package com.log3900.game.match.ffa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.log3900.R
import com.log3900.game.match.ActiveMatchFragment
import com.log3900.game.match.ActiveMatchPresenter

class ActiveFFAMatchFragment : ActiveMatchFragment(), ActiveFFAMatchView {
    private var activeFFAMatchPresenter: ActiveFFAMatchPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_ffa_match, container, false)

        setupUI(rootView)

        activeFFAMatchPresenter = ActiveFFAMatchPresenter(this)
        activeMatchPresenter = activeFFAMatchPresenter

        return rootView
    }

    override fun onDestroy() {
        activeFFAMatchPresenter = null
        super.onDestroy()
    }
}