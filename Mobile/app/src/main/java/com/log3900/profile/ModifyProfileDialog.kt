package com.log3900.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.log3900.R
import com.log3900.shared.ui.ProfileView
import com.log3900.user.Account
import com.log3900.user.AccountRepository
import com.log3900.utils.ui.getAccountAvatarID
import com.log3900.utils.ui.getAvatarID

class ModifyProfileDialog : DialogFragment(), ProfileView, ModifyAvatarDialogLauncher {
    lateinit var modifyProfilePresenter: ModifyProfilePresenter

    lateinit var originalAccount: Account
    var avatarIndex: Int = -1
    lateinit var avatarView: ImageView
    lateinit var usernameInput: TextInputEditText
    lateinit var passwordInput: TextInputEditText
    lateinit var emailInput: TextInputEditText
    lateinit var firstnameInput: TextInputEditText
    lateinit var lastnameInput: TextInputEditText
    lateinit var updateBtn: MaterialButton

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
        modifyProfilePresenter = ModifyProfilePresenter(this)
        originalAccount = AccountRepository.getAccount()
        avatarIndex = originalAccount.pictureID
        setUpUi(rootView)
        return rootView
    }

    private fun setUpUi(root: View) {
        val modifyAvatarBtn = root.findViewById<MaterialButton>(R.id.modify_avatar_button)
        modifyAvatarBtn.setOnClickListener {
            ModifyAvatarDialog.start(this, activity!!)
        }

        val cancelBtn = root.findViewById<MaterialButton>(R.id.cancel_modify_button)
        cancelBtn.setOnClickListener {
            dismiss()
        }
        val defaultBtn = root.findViewById<MaterialButton>(R.id.default_button)
        defaultBtn.setOnClickListener {
            it.clearFocus()
            fillDefaultDialogFields(root)
        }

        updateBtn = root.findViewById(R.id.update_button)
        updateBtn.setOnClickListener {
            sendModifiedInfo()
        }

        fillDefaultDialogFields(root)
    }

    fun sendModifiedInfo() {
        val password =
            if (passwordInput.text.toString() != resources.getString(R.string.password_asterisks))
                passwordInput.text.toString()
            else
                null

        val updatedAccount = Account(
            originalAccount.userID,
            usernameInput.text.toString(),
            avatarIndex,
            emailInput.text.toString(),
            firstnameInput.text.toString(),
            lastnameInput.text.toString(),
            originalAccount.sessionToken,
            originalAccount.bearerToken
        )
        modifyProfilePresenter.updateAccountInfo(updatedAccount, password)
    }


    /**
     * Fills the dialog with current account info
     */
    private fun fillDefaultDialogFields(root: View) {
        avatarView = root.findViewById(R.id.current_avatar)
        avatarView.setImageResource(getAccountAvatarID(originalAccount))

        usernameInput = root.findViewById<TextInputEditText>(R.id.username_input).apply {
            setText(originalAccount.username)
            doAfterTextChanged {
                modifyProfilePresenter.validateUsername(text.toString())
                enableUpdateIfAllValid()
            }
        }

        passwordInput = root.findViewById<TextInputEditText>(R.id.password_input).apply {
            setText(R.string.password_asterisks)
            doAfterTextChanged {
                modifyProfilePresenter.validatePassword(text.toString())
                enableUpdateIfAllValid()
            }
            setOnFocusChangeListener { _, _ ->
                val allAsterisks = resources.getString(R.string.password_asterisks)
                if (text.toString() == allAsterisks) {
                    text?.clear()
                }
                else if (text.toString().isEmpty()) {
                    setText(allAsterisks)
                }            }
        }

        emailInput = root.findViewById<TextInputEditText>(R.id.email_input).apply {
            setText(originalAccount.email)
            doAfterTextChanged {
                modifyProfilePresenter.validateEmail(text.toString())
                enableUpdateIfAllValid()
            }
        }

        firstnameInput = root.findViewById<TextInputEditText>(R.id.firstname_input).apply {
            setText(originalAccount.firstname)
            doAfterTextChanged {
                modifyProfilePresenter.validateFirstname(text.toString())
                enableUpdateIfAllValid()
            }
        }

        lastnameInput = root.findViewById<TextInputEditText>(R.id.lastname_input).apply {
            setText(originalAccount.lastname)
            doAfterTextChanged {
                modifyProfilePresenter.validateLastname(text.toString())
                enableUpdateIfAllValid()
            }
        }
    }

    override fun onAvatarChanged(avatarIndex: Int) {
        println("Avatar changed: ${this.avatarIndex} -> $avatarIndex")
        avatarView.setImageResource(getAvatarID(avatarIndex))
        this.avatarIndex = avatarIndex
        enableUpdateIfAllValid()
    }

    /**
     * Changes if the "Apply changes" button is enabled
     */
    private fun enableUpdateIfAllValid() {
        val username    = usernameInput.text.toString()
        val password    = passwordInput.text.toString()
        val email       = emailInput.text.toString()
        val firstname   = firstnameInput.text.toString()
        val lastname    = lastnameInput.text.toString()

        updateBtn.isEnabled =
                // At least 1 field has changed
            ((username != originalAccount.username)
                    || (password != resources.getString(R.string.password_asterisks))
                    || (email != originalAccount.email)
                    || (firstname != originalAccount.firstname)
                    || (lastname != originalAccount.lastname)
                    || (avatarIndex != originalAccount.pictureID))
                    // All fields are valid
                    && modifyProfilePresenter.validateUsername(username)
                    && modifyProfilePresenter.validatePassword(password)
                    && modifyProfilePresenter.validateEmail(email)
                    && modifyProfilePresenter.validateFirstname(firstname)
                    && modifyProfilePresenter.validateLastname(lastname)
    }

    override fun setUsernameError(error: String?) {
        usernameInput.error = error
    }
    override fun setPasswordError(error: String?) {
        passwordInput.error = error
    }
    override fun setEmailError(error: String?) {
        emailInput.error = error
    }
    override fun setFirstnameError(error: String?) {
        firstnameInput.error = error
    }
    override fun setLastnameError(error: String?) {
        lastnameInput.error = error
    }

    fun onModifySuccess(updatedAccount: Account) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Info modified")
            .setMessage("Account information modified with success!")
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .show()

        originalAccount = updatedAccount
        fillDefaultDialogFields(view!!)
    }

    fun onModifyError(error: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Error")
            .setMessage("Error: $error")
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}