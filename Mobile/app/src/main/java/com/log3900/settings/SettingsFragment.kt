package com.log3900.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.log3900.R
import com.log3900.settings.language.LanguageManager
import com.log3900.settings.language.LanguagePickerDialog
import com.log3900.settings.theme.ThemeManager
import com.log3900.settings.theme.ThemePickerFragment

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)

        findPreference<Preference>("language")?.summary = resources.getString(LanguageManager.getCurrentLanguage().stringResource)
        findPreference<Preference>("theme")?.summary = resources.getString(ThemeManager.getCurrentTheme().titleStringResource)

        setClickListeners()
    }

    private fun setClickListeners() {
        findPreference<Preference>("theme")?.setOnPreferenceClickListener {
            ThemePickerFragment().show(fragmentManager!! , "ThemePickerFragment")
            true
        }

        findPreference<Preference>("language")?.setOnPreferenceClickListener {
            LanguagePickerDialog().show(fragmentManager!!, "LanguagePickerDialog")
            true
        }
    }
}