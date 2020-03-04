package com.log3900.game.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.game.group.Group
import java.util.*

class MatchLobbyFragment : Fragment(), MatchLobbyView {
    private var matchLobbyPresenter: MatchLobbyPresenter? = null

    // UI
    private lateinit var matchesRecyclerView: RecyclerView
    private lateinit var matchesAdapter: MatchAdapter
    private lateinit var createMatchButton: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_match_lobby, container, false)

        setupUiElements(rootView)

        matchLobbyPresenter = MatchLobbyPresenter(this)

        return rootView
    }

    private fun setupUiElements(rootView: View) {
        setupRecyclerView(rootView)

        createMatchButton = rootView.findViewById(R.id.fragment_match_lobby_button_create_match)

        createMatchButton.setOnClickListener { matchLobbyPresenter?.onCreateMatchClicked() }

    }

    private fun setupRecyclerView(rootView: View) {
        matchesRecyclerView = rootView.findViewById(R.id.fragment_match_lobby_recycler_view_matches)
        matchesAdapter = MatchAdapter(arrayListOf(
            Group(UUID.randomUUID(), "Game 1", 8, 1, 2, 2, 1, UUID.randomUUID(), arrayListOf(UUID.randomUUID(), UUID.randomUUID())),
            Group(UUID.randomUUID(), "Game 2", 8, 1, 1, 2, 1, UUID.randomUUID(), arrayListOf(UUID.randomUUID())),
            Group(UUID.randomUUID(), "Game 3", 6, 1, 2, 2, 1, UUID.randomUUID(), arrayListOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
            ))

        matchesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = matchesAdapter
        }
    }


    override fun showMatchCreationDialog() {
        MatchCreationDialogFragment().show(fragmentManager!!, "MatchCreationDialogFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        matchLobbyPresenter?.destroy()
        matchLobbyPresenter = null
    }
}