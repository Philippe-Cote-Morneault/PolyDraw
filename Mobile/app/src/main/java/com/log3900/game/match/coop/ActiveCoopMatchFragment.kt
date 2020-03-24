package com.log3900.game.match.coop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.log3900.R
import com.log3900.game.match.ActiveMatchFragment
import com.log3900.game.match.ffa.ActiveFFAMatchPresenter
import com.log3900.game.match.ffa.ActiveFFAMatchView

class ActiveCoopMatchFragment : ActiveMatchFragment(), ActiveCoopMatchView {
    private var activeCoopMatchPresenter: ActiveCoopMatchPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_coop_match, container, false)

        setupUI(rootView)

        activeCoopMatchPresenter = ActiveCoopMatchPresenter(this)
        activeMatchPresenter = activeCoopMatchPresenter

        return rootView
    }

    override fun onDestroy() {
        activeCoopMatchPresenter = null
        super.onDestroy()
    }
}