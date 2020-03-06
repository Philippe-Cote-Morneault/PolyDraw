package com.log3900.game.waiting_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player

class MatchWaitingRoomFragment : Fragment(), MatchWaitingRoomView {
    private var matchWaitingRoomPresenter: MatchWaitingRoomPresenter? = null

    // UI
    private lateinit var startMatchButton: MaterialButton
    private lateinit var leaveMatchButton: MaterialButton
    private lateinit var matchNameTextView: TextView
    private lateinit var matchModeTextView: TextView
    private lateinit var hostNameTextView: TextView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var playersAdapter: PlayerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_match_waiting_room, container, false)

        setupUi(rootView)

        matchWaitingRoomPresenter = MatchWaitingRoomPresenter(this)

        return rootView
    }

    private fun setupUi(rootView: View) {
        startMatchButton = rootView.findViewById(R.id.fragment_match_waiting_room_button_start_match)
        leaveMatchButton = rootView.findViewById(R.id.fragment_match_waiting_room_button_leave_match)
        matchNameTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_match_name)
        matchModeTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_match_mode)
        hostNameTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_host_name)
        playersRecyclerView = rootView.findViewById(R.id.fragment_match_waiting_room_recycler_view_players)

        setupRecyclerView()

        setupUIListeners()
    }

    private fun setupUIListeners() {
        leaveMatchButton.setOnClickListener {
            matchWaitingRoomPresenter?.onLeaveMatchClick()
        }
    }

    private fun setupRecyclerView() {
        playersAdapter = PlayerAdapter(arrayListOf(), object : PlayerViewHolder.Listener {
            override fun playerClicked(player: Player) {

            }
        })
        playersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = playersAdapter
        }
    }

    override fun setGroup(group: Group) {
        matchNameTextView.text = group.groupName
        matchModeTextView.setText(MatchMode.stringRes(group.gameType))
        hostNameTextView.text = group.ownerName
    }

    override fun setPlayers(players: ArrayList<Player>) {
        playersAdapter.players = players
        playersAdapter.notifyDataSetChanged()
    }
    override fun onDestroy() {
        matchWaitingRoomPresenter?.destroy()
        matchWaitingRoomPresenter = null
        super.onDestroy()
    }
}