package com.log3900.game.match.ffa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Player
import com.log3900.game.match.ActiveMatchFragment
import com.log3900.game.match.ActiveMatchPresenter
import com.log3900.game.match.PlayerAdapter
import com.log3900.game.match.PlayerViewHolder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ActiveFFAMatchFragment : ActiveMatchFragment(), ActiveFFAMatchView {
    private var activeFFAMatchPresenter: ActiveFFAMatchPresenter? = null
    private lateinit var playersAdapter: PlayerAdapter

    // UI
    private lateinit var turnsTextView: TextView
    private lateinit var playersRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_ffa_match, container, false)

        setupUI(rootView)

        activeFFAMatchPresenter = ActiveFFAMatchPresenter(this)
        activeMatchPresenter = activeFFAMatchPresenter

        return rootView
    }

    override fun setupToolbar(rootView: View) {
        super.setupToolbar(rootView)
        turnsTextView = toolbar.findViewById(R.id.toolbar_active_match_text_view_rounds)
    }

    override fun setupHumanPlayerRecyclerView(rootView: View) {
        playersRecyclerView = rootView.findViewById(R.id.fragment_active_ffa_match_recycler_view_player_list)
        playersAdapter = PlayerAdapter()
        playersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = playersAdapter
        }
    }

    override fun setPlayers(players: ArrayList<Player>) {
        playersAdapter.setPlayers(players)
        playersAdapter.notifyDataSetChanged()
    }

    override fun setPlayerStatus(playerID: UUID, statusImageRes: Int) {
        playersAdapter.setPlayerStatusRes(playerID, statusImageRes)
    }

    override fun setPlayerScores(playerScores: HashMap<UUID, Int>) {
        playersAdapter.setPlayerScores(playerScores)
    }

    override fun clearAllPlayerStatusRes() {
        playersAdapter.resetAllImageRes()
        playersAdapter.notifyDataSetChanged()
    }

    override fun notifyPlayersChanged() {
        playersAdapter.notifyDataSetChanged()
    }

    override fun setTurnsValue(turns: String) {
        turnsTextView.text = turns
    }

    override fun showPlayerScoredChangedAnimation(scoreChangedValue: String, isPositive: Boolean, position: Int) {
        val view = playersAdapter.getViewHolderAtPos(position)
        view?.showScoreChangedAnimation(scoreChangedValue, isPositive)
    }

    override fun onDestroy() {
        activeFFAMatchPresenter = null
        super.onDestroy()
    }
}