package com.log3900.game.match.UI

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.MainApplication
import com.log3900.R

class RoundEndInfoView(context: Context) : ConstraintLayout(context) {
    private var layout: ConstraintLayout
    private var wordTextView: TextView
    private var playersRecyclerView: RecyclerView

    private lateinit var playersAdapter: PlayerAdapter

    init {
        layout = View.inflate(context, R.layout.view_word_guessing, this) as ConstraintLayout
        wordTextView = layout.findViewById(R.id.view_round_end_info_text_view_word)
        playersRecyclerView = layout.findViewById(R.id.view_round_end_info_recycler_view_players)

        setupRecyclerView()
    }

    fun setupRecyclerView() {
        playersAdapter = PlayerAdapter()
        playersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = playersAdapter
        }
    }

    fun setWord(word: String) {
        wordTextView.text = MainApplication.instance.getContext().getString(R.string.round_end_info_word_was_sentence, word)
    }

    fun setPlayers(players: ArrayList<Pair<String, Int>>) {
        playersAdapter.setPlayers(players)
    }

    class PlayerAdapter : RecyclerView.Adapter<PlayerViewHolder>() {
        private var players: ArrayList<Pair<String, Int>> = arrayListOf()

        fun setPlayers(players: ArrayList<Pair<String, Int>>) {
            this.players = players
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return players.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_round_end_info_player, parent, false)

            return PlayerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
            holder.bind(players[position], position + 1)
        }
    }


    class PlayerViewHolder : RecyclerView.ViewHolder {
        private var rootView: View
        private var positionTextView: TextView
        private var nameTextView: TextView
        private var scoreTextView: TextView

        private lateinit var player: Pair<String, Int>

        constructor(itemView: View) : super(itemView) {
            rootView = itemView
            positionTextView = itemView.findViewById(R.id.list_item_round_end_info_text_view_position)
            nameTextView = itemView.findViewById(R.id.list_item_round_end_info_text_view_name)
            scoreTextView = itemView.findViewById(R.id.list_item_round_end_info_text_view_points)
        }

        fun bind(player: Pair<String, Int>, position: Int) {
            this.player = player
            positionTextView.text = "#$position"
            nameTextView.text = player.first

            if (player.second > 0) {
                scoreTextView.text = "+${player.second}"
                scoreTextView.setTextColor(Color.GREEN)

            } else if (player.second == 0) {
                scoreTextView.text = "+0"
                scoreTextView.setTextColor(Color.WHITE)
            }
        }
    }
}