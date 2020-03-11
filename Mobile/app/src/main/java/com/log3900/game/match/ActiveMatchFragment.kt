package com.log3900.game.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Player
import com.log3900.game.match.UI.WordGuessingView
import java.util.*
import kotlin.collections.ArrayList

class ActiveMatchFragment : Fragment(), ActiveMatchView {
    private var activeMatchPresenter: ActiveMatchPresenter? = null
    private lateinit var playersAdapter: PlayerAdapter

    // UI
    private lateinit var guessingView: WordGuessingView
    private lateinit var playersRecyclerView: RecyclerView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_active_match, container, false)

        setupUI(rootView)

        activeMatchPresenter = ActiveMatchPresenter(this)

        return rootView
    }

    private fun setupUI(rootView: View) {
        guessingView = rootView.findViewById(R.id.fragment_active_match_guess_container)
        playersRecyclerView = rootView.findViewById(R.id.fragment_active_match_recycler_view_player_list)

        guessingView.listener = object : WordGuessingView.Listener {
            override fun onGuessPressed(text: String) {
                activeMatchPresenter?.guessPressed(text)
            }
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
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

    override fun clearAllPlayerStatusRes() {
        playersAdapter.resetAllImageRes()
        playersAdapter.notifyDataSetChanged()
    }

    override fun setWordToGuessLength(length: Int) {
        guessingView.setWordLength(length)
    }

    override fun onDestroy() {
        activeMatchPresenter?.destroy()
        activeMatchPresenter = null
        super.onDestroy()
    }
}