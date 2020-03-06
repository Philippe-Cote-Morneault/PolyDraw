package com.log3900.game.waiting_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.log3900.R

class MatchWaitingRoomFragment : Fragment(), MatchWaitingRoomView {
    private var matchWaitingRoomPresenter: MatchWaitingRoomPresenter? = null

    // UI
    private lateinit var startMatchButton: MaterialButton
    private lateinit var leaveMatchButton: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_match_waiting_room, container, false)

        setupUi(rootView)

        matchWaitingRoomPresenter = MatchWaitingRoomPresenter(this)

        return rootView
    }

    private fun setupUi(rootView: View) {
        startMatchButton = rootView.findViewById(R.id.fragment_match_waiting_room_button_start_match)
        leaveMatchButton = rootView.findViewById(R.id.fragment_match_waiting_room_button_leave_match)

        setupUIListeners()
    }

    private fun setupUIListeners() {
        leaveMatchButton.setOnClickListener {
            matchWaitingRoomPresenter?.onLeaveMatchClick()
        }
    }

    override fun onDestroy() {
        matchWaitingRoomPresenter?.destroy()
        matchWaitingRoomPresenter = null
        super.onDestroy()
    }
}