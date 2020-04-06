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
import com.log3900.user.account.AccountRepository
import com.log3900.utils.format.DateFormatter
import com.log3900.utils.format.formatDuration
import java.util.*

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

        val matchDurationDate = Date(match.duration)
        holder.matchDuration.text = DateFormatter.formatFullTime(matchDurationDate)

        holder.itemView.setOnClickListener {
            startMatchDetailsDialog(match)
        }

        if (match.matchType == "FFA") {
            holder.setStyleFromMatchResult(match)
        }
    }

    private fun ViewHolder.setStyleFromMatchResult(match: GamePlayed) {
        val matchWon = (AccountRepository.getInstance().getAccount().ID.toString() == match.winnerID)
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

        matchResult.text =
            if (matchWon)
                "WIN"
            else
                "LOSS"

        matchResult.setTextColor(matchResultColorText)
        setBackgroundColor(matchResultColorBackground)
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