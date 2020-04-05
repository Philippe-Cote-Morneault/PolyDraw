package com.log3900.profile.stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.utils.format.DateFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.round

class ProfileStatsFragment : Fragment() {

    private val profileStatsPresenter = ProfileStatsPresenter(this)
    lateinit var connectionHistoryButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile_stats, container, false)

        setUpUI(root)
        fetchStats()

        return root
    }

    private fun setUpUI(root: View) {
        connectionHistoryButton = root.findViewById(R.id.connection_history_button)
        connectionHistoryButton.setOnClickListener {
            showConnectionHistoryDialog()
        }
    }

    private fun fetchStats() {
        println("Fetching stats!")
        profileStatsPresenter.fetchStats()
    }

    fun showStats(userStats: UserStats) {
        view?.let {
            val gamesPlayed: TextView       = it.findViewById(R.id.games_played_stat)
            val winRatio: TextView          = it.findViewById(R.id.win_ratio_stat)
            val avgGameDuration: TextView   = it.findViewById(R.id.avg_game_duration_stat)
            val timePlayed: TextView        = it.findViewById(R.id.time_played_stat)
            val bestSoloScore: TextView     = it.findViewById(R.id.best_solo_score_stat)

            gamesPlayed.text = userStats.gamesPlayed.toString()

            val winRatioValue = BigDecimal(round(userStats.winRatio * 10000) / 100)
                .setScale(2, RoundingMode.HALF_EVEN)
            winRatio.text = "$winRatioValue%"

            val avgGameDurationDate = Date(round(userStats.averageGameDuration).toLong())
            Log.d("STATS_PROFILE", avgGameDurationDate.time.toString())
            avgGameDuration.text = DateFormatter.formatDateToTime(avgGameDurationDate)

            timePlayed.text = userStats.timePlayed.toString()
            bestSoloScore.text = userStats.bestScoreSolo.toString()
        }
    }

    fun showConnectionHistoryDialog() {
        val fragmentManager = activity?.supportFragmentManager!!
        val ft = fragmentManager.beginTransaction()
        fragmentManager.findFragmentByTag("dialog")?.let {
            ft.remove(it)
        }
        ft.addToBackStack(null)

        val connectionHistDialog = ConnectionHistoryDialog()
        connectionHistDialog.show(ft, "dialog")
    }
}