package com.log3900.profile.stats

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.R
import kotlinx.coroutines.*

class ConnectionHistoryDialog : DialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_connection_history, container, false)
        setUpUi(rootView)
        return rootView
    }

    private fun setUpUi(root: View) {
        val closeButton: MaterialButton = root.findViewById(R.id.close_dialog_button)
        closeButton.setOnClickListener {
            dismiss()
        }

        setUpMessagesRV(root)
    }

    private fun setUpMessagesRV(root: View) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val rvConnections: RecyclerView = root.findViewById(R.id.rv_connections)
//                    val connections = StatsRepository.getConnectionHistory()
                val connections = getConnectionHistory()
                val connectionAdapter = ConnectionAdapter(connections)
                rvConnections.apply {
                    adapter = connectionAdapter
                    layoutManager = LinearLayoutManager(activity)
                }
            }
        }
    }

    private suspend fun getConnectionHistory(): List<Connection> {
        // TODO: Error handling
        return StatsRepository.getConnectionHistory()
    }
}