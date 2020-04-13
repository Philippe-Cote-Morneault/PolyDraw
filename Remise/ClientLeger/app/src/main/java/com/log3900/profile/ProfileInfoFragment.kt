package com.log3900.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.log3900.R
import com.log3900.databinding.FragmentProfileInfoBinding
import com.log3900.user.account.Account
import com.log3900.user.account.AccountRepository
import com.log3900.utils.ui.getAccountAvatarID

class ProfileInfoFragment : Fragment() {
    private lateinit var infoBinding: FragmentProfileInfoBinding
    lateinit var avatarView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        infoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_info, container, false)
        infoBinding.info = AccountRepository.getInstance().getAccount()

        setUpUi(infoBinding.root)

        return infoBinding.root
    }

    private fun setUpUi(root: View) {
        // Clickable ImageView, acts as button
        val modifyButton = root.findViewById<ImageView>(R.id.modify_button)
        modifyButton.setOnClickListener { showModifyDialog() }

        avatarView = root.findViewById(R.id.avatar_info)
        avatarView.setImageResource(getAccountAvatarID(infoBinding.info!!))
    }

    fun updateProfileInfo(updatedAccount: Account) {
        infoBinding.info = updatedAccount
        avatarView.setImageResource(getAccountAvatarID(updatedAccount))
        infoBinding.invalidateAll()
    }

    private fun showModifyDialog() {
        val fragmentManager = activity?.supportFragmentManager!!
        val ft = fragmentManager.beginTransaction()
        fragmentManager.findFragmentByTag("dialog")?.let {
            ft.remove(it)
        }
        ft.addToBackStack(null)

        val modifyDialogFragment = ModifyProfileDialog(this)
        modifyDialogFragment.show(ft, "dialog")
    }
}