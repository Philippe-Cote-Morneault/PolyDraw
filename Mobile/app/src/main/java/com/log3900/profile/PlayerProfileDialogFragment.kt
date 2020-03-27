package com.log3900.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.log3900.R
import java.util.*

class PlayerProfileDialogFragment(userID: UUID) : DialogFragment() {
    val presenter = PlayerProfilePresenter(this, userID)

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_player_profile, container, false)
        setUpUi(rootView)
        return rootView
    }

    private fun setUpUi(root: View) {
        presenter.fetchUserInfo()
    }
}