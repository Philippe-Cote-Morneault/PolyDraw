package com.log3900.profile.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.log3900.R

class ProfileStatsFragment : Fragment() {

    val profileStatsPresenter = ProfileStatsPresenter(this)
    lateinit var connectionHistoryButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile_stats, container, false)

        setUpUI(root)

        return root
    }

    private fun setUpUI(root: View) {
        connectionHistoryButton = root.findViewById(R.id.connection_history_button)
        connectionHistoryButton.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setMessage("clicked")
                .show()
        }

        fetchStats()
    }

    private fun fetchStats() {
        println("Fetching stats!")
        profileStatsPresenter.fetchStats()
    }
}