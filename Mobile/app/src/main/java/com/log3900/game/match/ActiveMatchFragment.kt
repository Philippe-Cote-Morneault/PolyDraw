package com.log3900.game.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.log3900.R

class ActiveMatchFragment : Fragment(), ActiveMatchView {
    private var activeMatchPresenter: ActiveMatchPresenter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_match, container, false)

        activeMatchPresenter = ActiveMatchPresenter(this)

        return rootView
    }


    override fun onDestroy() {
        activeMatchPresenter?.destroy()
        activeMatchPresenter = null
        super.onDestroy()
    }
}