package com.log3900.settings

import android.os.Bundle
import android.os.Handler
import android.util.Log
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
            ThemePickerFragment{
                Handler().postDelayed({
                    if (ThemeManager.hasActivityThemeChanged(activity!!)) {
                        activity?.recreate()
                    }
                }, 500)
            }.show(fragmentManager!! , "ThemePickerFragment")
            true
        }

        findPreference<Preference>("language")?.setOnPreferenceClickListener {
            LanguagePickerDialog{
                Handler().postDelayed({
                    if (LanguageManager.hasContextLanguageChanged(activity!!.baseContext)) {
                        activity?.recreate()
                    }
                }, 500)
            }.show(fragmentManager!!, "LanguagePickerDialog")
            true
        }
    }
}