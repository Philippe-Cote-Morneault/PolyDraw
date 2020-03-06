package com.log3900.game.waiting_room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

class PlayerAdapter: RecyclerView.Adapter<PlayerViewHolder> {
    private lateinit var players: ArrayList<Player>
    private var displayedPlayers: ArrayList<Boolean>
    private lateinit var group: Group
    private var listener: PlayerViewHolder.Listener

    constructor(listener: PlayerViewHolder.Listener) {
        this.listener = listener

        displayedPlayers = arrayListOf()
    }

    fun setGroup(group: Group) {
        this.group = group
        displayedPlayers = arrayListOf()

        for (i in 1..group.playersMax) {
            displayedPlayers.add(false)
        }
    }

    fun setPlayers(players: ArrayList<Player>) {
        this.players = players

        for (i in 0 until players.size) {
            displayedPlayers[i] = true
        }

        notifyDataSetChanged()
    }

    fun playerAdded(playerID: UUID) {
        displayedPlayers[players.size - 1] = true
        notifyDataSetChanged()
    }

    fun playerRemoved(playerID: UUID) {
        displayedPlayers[players.size - 1] = false
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return displayedPlayers.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_match_waiting_room_player, parent, false)

        return PlayerViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (displayedPlayers[position]) {
            holder.bind(players[position], false)
        } else {
            holder.bind(null, true)
        }
    }
}