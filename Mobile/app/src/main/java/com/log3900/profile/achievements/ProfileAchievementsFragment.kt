package com.log3900.profile.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.log3900.R

class ProfileAchievementsFragment : Fragment() {
    val presenter = ProfileAchievementsPresenter(this)

    lateinit var achievementsCount: TextView
    lateinit var achievementsSoFarText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile_achievements, container, false)

        setUpUI(root)

        return root
    }

    fun setUpUI(root: View) {
        achievementsCount = root.findViewById(R.id.achievements_count)
        achievementsSoFarText = root.findViewById(R.id.achievements_so_far)
        presenter.fetchAchievements()
    }

    fun updateAchievementsCount(count: Int) {
        achievementsCount.text = count.toString()

        // 'achievement' to plural or singular
        achievementsSoFarText.text = resources.getString(
            if (count == 1)
                R.string.so_far_message_singular
            else
                R.string.so_far_message
        )
    }
}