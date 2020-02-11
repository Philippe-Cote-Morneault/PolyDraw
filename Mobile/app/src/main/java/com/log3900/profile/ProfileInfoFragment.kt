package com.log3900.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.log3900.R

class ProfileInfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("hello from fragment")
        val root = inflater.inflate(R.layout.fragment_profile_info, container, false)
        return root
    }
}