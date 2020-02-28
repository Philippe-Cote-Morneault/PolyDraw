package com.log3900.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.log3900.R
import com.log3900.settings.theme.ThemePickerFragment

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