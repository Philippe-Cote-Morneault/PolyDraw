package com.log3900.game.match.solo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Player
import com.log3900.game.match.ActiveMatchFragment
import com.log3900.game.match.coop.TeamPlayerAdapter

class ActiveSoloMatchFragment : ActiveMatchFragment(), ActiveSoloMatchView {
    private var activeSoloMatchPresenter: ActiveSoloMatchPresenter? = null
    private lateinit var teamPlayersAdapter: TeamPlayerAdapter

    // UI
    protected lateinit var teamPlayersRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_solo_match, container, false)

        setupUI(rootView)

        activeSoloMatchPresenter = ActiveSoloMatchPresenter(this)
        activeMatchPresenter = activeSoloMatchPresenter

        return rootView
    }

    override fun setupHumanPlayerRecyclerView(rootView: View) {
        teamPlayersAdapter = TeamPlayerAdapter()
        teamPlayersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = teamPlayersAdapter
        }
    }

    override fun setPlayers(players: ArrayList<Player>) {
        teamPlayersAdapter.setPlayers(players)
        teamPlayersAdapter.notifyDataSetChanged()
    }

    override fun notifyPlayersChanged() {
        teamPlayersAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        activeSoloMatchPresenter = null
        super.onDestroy()
    }
}