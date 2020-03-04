package com.log3900.game.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class MatchLobbyFragment : Fragment(), MatchLobbyView {
    private lateinit var matchLobbyPresenter: MatchLobbyPresenter

    // UI
    private lateinit var matchesRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_match_lobby, container, false)

        setupUiElements(rootView)

        matchLobbyPresenter = MatchLobbyPresenter(this)

        return rootView
    }

    private fun setupUiElements(rootView: View) {
        matchesRecyclerView = rootView.findViewById(R.id.fragment_match_lobby_recycler_view_matches)
    }

}