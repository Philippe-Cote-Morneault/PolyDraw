package com.log3900.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.log3900.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        println("Creating SettingsFragment")
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)

        setClickListeners()
    }

    private fun setClickListeners() {
        findPreference<Preference>("theme")?.setOnPreferenceClickListener {
            println("Clicked on themes!")
            ThemePickerFragment().show(fragmentManager!! , "ThemePickerFragment")
            true
        }
    }
}