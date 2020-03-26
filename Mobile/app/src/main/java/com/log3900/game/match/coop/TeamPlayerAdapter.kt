package com.log3900.game.match.coop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.game.group.Player
import java.util.*
import kotlin.collections.ArrayList

class TeamPlayerAdapter: RecyclerView.Adapter<TeamPlayerViewHolder>() {
    private var players: ArrayList<Player> = arrayListOf()
    private var playersWithoutCPU: ArrayList<Player> = arrayListOf()

    fun setPlayers(players: ArrayList<Player>) {
        this.players = players

        refreshLists()
    }

    fun playerAdded(playerID: UUID) {
        refreshLists()
    }

    fun playerRemoved(playerID: UUID) {
        refreshLists()
    }

    private fun refreshLists() {
        playersWithoutCPU.clear()
        players.forEach {
           if (!it.isCPU) {
               playersWithoutCPU.add(it)
           }
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return playersWithoutCPU.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamPlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_active_match_team_player, parent, false)

        return TeamPlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamPlayerViewHolder, position: Int) {
        holder.bind(playersWithoutCPU[position])
    }
}