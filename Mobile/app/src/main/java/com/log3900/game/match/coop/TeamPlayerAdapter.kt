package com.log3900.game.match.coop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Player

class TeamPlayerAdapter: RecyclerView.Adapter<TeamPlayerViewHolder>() {
    private var players: ArrayList<Player> = arrayListOf()

    fun setPlayers(players: ArrayList<Player>) {
        this.players = players
    }

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamPlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_active_match_team_player, parent, false)

        return TeamPlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamPlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }
}