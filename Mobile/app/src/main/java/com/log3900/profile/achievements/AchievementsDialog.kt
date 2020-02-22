package com.log3900.profile.achievements

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.log3900.R

class AchievementsDialog : DialogFragment() {
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

        setUpAchievementsRecyclerView(root)
    }

    private fun setUpAchievementsRecyclerView(root: View) {
        // TODO: Populate recycler view
    }
}