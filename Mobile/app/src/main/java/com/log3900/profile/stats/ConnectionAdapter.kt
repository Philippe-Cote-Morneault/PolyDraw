package com.log3900.profile.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R

class ConnectionAdapter(val connections: List<Connection>)
    : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var connectionTimeText: TextView = itemView.findViewById(R.id.connected_time)
        var disconnectionTimeText: TextView = itemView.findViewById(R.id.disconnected_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val connectionView = inflater.inflate(R.layout.list_item_connection, parent, false)
        return ViewHolder(connectionView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val connection = connections.get(position)

        holder.connectionTimeText.text = connection.connectedAt.toString()
        holder.disconnectionTimeText.text = connection.disconnectedAt.toString()
    }

    override fun getItemCount(): Int = connections.size
}