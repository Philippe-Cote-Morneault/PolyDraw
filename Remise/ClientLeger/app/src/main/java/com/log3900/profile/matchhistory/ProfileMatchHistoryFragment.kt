package com.log3900.profile.matchhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.profile.stats.GamePlayed
import com.log3900.profile.stats.StatsRepository
import com.log3900.user.account.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileMatchHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_match_history, container, false)

        setUpUI(root)

        return root
    }

    private fun setUpUI(root: View) {
        setUpMatchesPlayedRecyclerView(root)
    }

    private fun setUpMatchesPlayedRecyclerView(root: View) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val rvMatches: RecyclerView = root.findViewById(R.id.rv_matches)
                val matches = getMatchesPlayed()
                val matchesAdapter = MatchPlayedAdapter(matches, AccountRepository.getAccount().blockingGet().username, rvMatches)

                rvMatches.apply {
                    adapter = matchesAdapter
                    layoutManager = LinearLayoutManager(activity)
                    addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                }
            }
        }
    }

    private suspend fun getMatchesPlayed(): List<GamePlayed> {
        // TODO: Error handling
        return StatsRepository.getGamesPlayedHistory()
    }

}