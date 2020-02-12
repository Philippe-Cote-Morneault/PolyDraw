package com.log3900.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.log3900.R
import com.log3900.databinding.FragmentProfileInfoBinding
import com.log3900.profile.models.ProfileInfo

class ProfileInfoFragment : Fragment() {
    private lateinit var infoBinding: FragmentProfileInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        infoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_info, container, false)
        infoBinding.info = ProfileInfo("myusername", "mypassword123", "Myname", "My-Lastname", "my@email.com")

        return infoBinding.root
    }
}