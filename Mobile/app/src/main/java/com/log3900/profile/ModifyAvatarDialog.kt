package com.log3900.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.login.LoginActivity
import com.log3900.settings.LocaleLanguageHelper
import com.log3900.utils.ui.getAvatarID
import kotlinx.android.synthetic.main.dialog_modify_avatar.*


interface ModifyAvatarDialogLauncher {
    fun onAvatarChanged(avatarIndex: Int)
}

class ModifyAvatarDialog(private val launcher: ModifyAvatarDialogLauncher) : DialogFragment() {

    companion object {
        fun start(launcher: ModifyAvatarDialogLauncher, activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            val ft = fragmentManager.beginTransaction()
            fragmentManager.findFragmentByTag("avatar_dialog")?.let {
                ft.remove(it)
            }
            ft.addToBackStack(null)

            val avatarDialogFragment = ModifyAvatarDialog(launcher)
            avatarDialogFragment.show(ft, "avatar_dialog")
        }
    }

    var selectedAvatarID = -1
    lateinit var selectedAvatarView: ImageView
    lateinit var selectedTitle: TextView
    lateinit var confirmButton: MaterialButton

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_modify_avatar, container, false)
        setUpUi(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (activity is LoginActivity) {
            changeResLanguage((activity as LoginActivity).currentLanguageCode)
        }
    }

    private fun setUpUi(root: View) {
        val closeButton: MaterialButton = root.findViewById(R.id.close_dialog_button)
        closeButton.setOnClickListener {
            dismiss()
        }
        confirmButton = root.findViewById(R.id.confirm_dialog_button)
        confirmButton.setOnClickListener {
            launcher.onAvatarChanged(selectedAvatarID)
            dismiss()
        }

        selectedAvatarView = root.findViewById(R.id.selected_avatar)
        selectedTitle = root.findViewById(R.id.selected_title)

        val avatarContainer: GridLayout = root.findViewById(R.id.avatar_container)
        avatarContainer.children
            .sortedBy { it.id }
            .forEachIndexed { index, avatar ->
                avatar.setOnClickListener {
                    changeSelectedAvatar(index)
                }
        }
    }

    private fun changeSelectedAvatar(index: Int) {
        selectedTitle.visibility = View.VISIBLE
        selectedAvatarView.visibility = View.VISIBLE
        confirmButton.isEnabled = true

        selectedAvatarID = index + 1
        selectedAvatarView.setImageResource(getAvatarID(selectedAvatarID))
    }

    private fun changeResLanguage(language: String) {
        LocaleLanguageHelper.getLocalizedResources(context!!, language).apply {
            modify_avatar_title.text = getString(R.string.avatar_choose)
            selected_title.text = getString(R.string.avatar_selected)
            confirm_dialog_button.text = getString(R.string.confirm)
            close_dialog_button.text = getString(R.string.close)
        }
    }
}