package com.log3900.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.log3900.R
import com.log3900.user.Account
import com.log3900.user.AccountRepository

class ModifyProfileDialog : DialogFragment() {
    lateinit var originalAccount: Account

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
        val rootView = inflater.inflate(R.layout.dialog_modify_profile, container, false)
        originalAccount = AccountRepository.getAccount()
        setUpUi(rootView)
        return rootView
    }

    private fun setUpUi(root: View) {
        val cancelBtn = root.findViewById<MaterialButton>(R.id.cancel_modify_button)
        cancelBtn.setOnClickListener {
            dismiss()
        }

        fillDefaultDialogFields(root)
    }

    /**
     * Fills the dialog with current account info
     */
    private fun fillDefaultDialogFields(root: View) {
        // TODO: Avatar
        val usernameInput = root.findViewById<TextInputEditText>(R.id.username_input)
        usernameInput.setText(originalAccount.username)
    }
}