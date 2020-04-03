package com.log3900.profile.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.utils.format.DateFormatter
import java.util.*

class ConnectionAdapter(val connections: List<Connection>)
    : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var connectionTimeText: TextView = itemView.findViewById(R.id.connected_time)
        var disconnectionTimeText: TextView = itemView.findViewById(R.id.disconnected_time)
        var timePlayedText: TextView = itemView.findViewById(R.id.time_played)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val connectionView = inflater.inflate(R.layout.list_item_connection, parent, false)
        return ViewHolder(connectionView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val connection = connections.get(position)

        val connectionDate = Date(connection.connectedAt.toLong()*1000)
        holder.connectionTimeText.text = DateFormatter.formatFullDate(connectionDate)//DateFormatter.formatDate(connectionDate)

        val disconnectionDate = Date(connection.disconnectedAt.toLong()*1000)
        holder.disconnectionTimeText.text = DateFormatter.formatFullDate(disconnectionDate)

        val timePlayedDate = Date(disconnectionDate.time - connectionDate.time)
        holder.timePlayedText.text = DateFormatter.formatFullTime(timePlayedDate)
    }

    override fun getItemCount(): Int = connections.size
}