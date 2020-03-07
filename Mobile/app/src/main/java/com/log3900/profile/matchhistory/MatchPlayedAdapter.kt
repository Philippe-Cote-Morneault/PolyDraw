package com.log3900.profile.matchhistory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.profile.stats.GamePlayed
import com.log3900.utils.format.formatDuration

class MatchPlayedAdapter(private val matchesPlayed: List<GamePlayed>,
                         private val username: String,
                         private val recyclerView: RecyclerView
) : RecyclerView.Adapter<MatchPlayedAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var matchType: TextView = itemView.findViewById(R.id.match_type)
        var matchResult: TextView = itemView.findViewById(R.id.match_result)
        var matchDuration: TextView = itemView.findViewById(R.id.match_duration)

        fun setBackgroundColor(color: Int) =
            itemView.findViewById<ConstraintLayout>(R.id.match_container).setBackgroundColor(color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)

        val matchView = inflater.inflate(R.layout.list_item_match_played, parent, false)
        return ViewHolder(matchView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = matchesPlayed[position]

        holder.matchType.text = match.matchType.toUpperCase()

        val matchWon = (username == match.winner)
        val matchResultColorBackground = ContextCompat.getColor(context,
            if (matchWon)
                R.color.color_win_background
            else
                R.color.color_loss_background
        )
        val matchResultColorText = ContextCompat.getColor(context,
            if (matchWon)
                R.color.color_win_text
            else
                R.color.color_loss_text
        )

        holder.matchResult.text =
            if (matchWon)
                "WIN"
            else
                "LOSS"
        holder.matchResult.setTextColor(matchResultColorText)

        holder.matchDuration.text = formatDuration(match.duration)

        holder.setBackgroundColor(matchResultColorBackground)

        holder.itemView.setOnClickListener {
            startMatchDetailsDialog(match)
        }
    }

    override fun getItemCount(): Int = matchesPlayed.size



    private fun startMatchDetailsDialog(match: GamePlayed) {
        val activity = context as FragmentActivity
        val fragmentManager = activity.supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        fragmentManager.findFragmentByTag("dialog")?.let {
            ft.remove(it)
        }
        ft.addToBackStack(null)

        val dialog = MatchDetailsDialog(match, username)
        dialog.show(ft, "dialog")
    }
}