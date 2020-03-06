package com.log3900.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.log3900.R

class ProfileFragment : Fragment() {

    private val profileVm: ProfileViewModel by lazy {
        ViewModelProviders.of(this).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//
//        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.MyTheme_DayNight)
//        val localInflater = inflater.cloneInContext(contextThemeWrapper)
//        val root = localInflater.inflate(R.layout.fragment_profile, container, false)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        return root
//        return infoBinding.root
    }
}