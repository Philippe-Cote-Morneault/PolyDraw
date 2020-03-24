package com.log3900.game.match.ffa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.log3900.R
import com.log3900.game.match.ActiveMatchFragment
import com.log3900.game.match.ActiveMatchPresenter

class ActiveFFAMatchFragment : ActiveMatchFragment(), ActiveFFAMatchView {
    private var activeFFAMatchPresenter: ActiveFFAMatchPresenter? = null

    // UI
    private lateinit var turnsTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_ffa_match, container, false)

        setupUI(rootView)

        activeFFAMatchPresenter = ActiveFFAMatchPresenter(this)
        activeMatchPresenter = activeFFAMatchPresenter

        return rootView
    }

    override fun setupUI(rootView: View) {
        super.setupUI(rootView)
    }

    override fun setupToolbar() {
        super.setupToolbar()
        turnsTextView = toolbar.findViewById(R.id.toolbar_active_match_text_view_rounds)
    }

    override fun setTurnsValue(turns: String) {
        turnsTextView.text = turns
    }

    override fun onDestroy() {
        activeFFAMatchPresenter = null
        super.onDestroy()
    }
}