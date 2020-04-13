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
import com.log3900.game.group.GroupCreated
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
        matchesAdapter = MatchAdapter(arrayListOf(), object : MatchViewHolder.Listener {
            override fun joinButtonClicked(group: Group) {
                matchLobbyPresenter?.onJoinMatchClick(group)
            }
        })

        matchesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = matchesAdapter
        }
    }


    override fun showMatchCreationDialog() {
        MatchCreationDialogFragment(object : MatchCreationDialogFragment.Listener {
            override fun onPositiveClick(groupCreated: GroupCreated) {
                matchLobbyPresenter?.onCreateMatch(groupCreated)
            }
        }).show(fragmentManager!!, "MatchCreationDialogFragment")
    }

    override fun setAvailableGroups(groups: ArrayList<Group>) {
        matchesAdapter.matches = groups
        matchesAdapter.notifyDataSetChanged()
    }

    override fun notifyMatchesChanged() {
        matchesAdapter.notifyDataSetChanged()
    }

    override fun groupUpdated(groupID: UUID) {
        val updatedGroupIndex = matchesAdapter.matches.indexOfFirst { it.ID == groupID }
        matchesAdapter.notifyItemChanged(updatedGroupIndex)
    }

    override fun onResume() {
        super.onResume()
        matchLobbyPresenter?.resume()
    }

    override fun onDestroy() {
        matchLobbyPresenter?.destroy()
        matchLobbyPresenter = null
        super.onDestroy()
    }
}