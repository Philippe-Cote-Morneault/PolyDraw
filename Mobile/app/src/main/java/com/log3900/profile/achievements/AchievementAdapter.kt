package com.log3900.profile.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.profile.stats.Achievement
import com.log3900.utils.format.DateFormatter
import java.util.*

class AchievementAdapter(val achievements: List<Achievement>)
    : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var achievementTitle: TextView = itemView.findViewById(R.id.achievement_title)
        var achievementDescription: TextView = itemView.findViewById(R.id.achievement_description)
        var achievementDate: TextView = itemView.findViewById(R.id.achievement_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val achievementView = inflater.inflate(R.layout.list_item_achievement, parent, false)
        return ViewHolder(achievementView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]

        holder.achievementTitle.text = achievement.title
        holder.achievementDescription.text = achievement.description

        val date = Date(achievement.unlockDate.toLong())    // TODO Update once date is changed
        holder.achievementDate.text = DateFormatter.formatDate(date)
    }

    override fun getItemCount(): Int = achievements.size
}