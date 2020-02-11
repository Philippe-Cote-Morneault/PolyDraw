package com.log3900.profile

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.log3900.R
import com.log3900.databinding.FragmentProfileInfoBinding
import com.log3900.profile.models.ProfileInfo

class ProfileFragment : Fragment() {

    private val profileVm: ProfileViewModel by lazy {
        ViewModelProviders.of(this).get(ProfileViewModel::class.java)
    }
    private lateinit var infoBinding: FragmentProfileInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        infoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_info, container, false)
        infoBinding.info = ProfileInfo("myusername", "mypassword123", "Myname", "My-Lastname", "my@email.com")
//
//        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.MyTheme_DayNight)
//        val localInflater = inflater.cloneInContext(contextThemeWrapper)
//        val root = localInflater.inflate(R.layout.fragment_profile, container, false)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        return root
//        return infoBinding.root
    }
}