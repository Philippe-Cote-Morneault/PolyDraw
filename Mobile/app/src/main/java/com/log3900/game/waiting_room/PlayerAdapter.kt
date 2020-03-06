package com.log3900.game.waiting_room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Player

class PlayerAdapter(var players: ArrayList<Player>, var listener: PlayerViewHolder.Listener) : RecyclerView.Adapter<PlayerViewHolder>() {

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_match_waiting_room_player, parent, false)

        return PlayerViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }
}