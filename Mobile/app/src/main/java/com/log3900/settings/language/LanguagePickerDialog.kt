package com.log3900.settings.language

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.log3900.MainApplication

class LanguagePickerDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var selectedLanguage = LanguageManager.getCurrentLanguage()
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle("Language Picker")
            .setPositiveButton("Save") { _, _ ->
                LanguageManager.changeLanguage(selectedLanguage).subscribe {
                    LanguageManager.applySavedLanguage(MainApplication.instance.baseContext)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->

            }
            .setSingleChoiceItems(languagesToCharArray(LanguageManager.getAvailableLanguages()), selectedLanguage.index) { _, pos ->
                selectedLanguage = LanguageManager.getAvailableLanguages()[pos]
            }
        return dialogBuilder.create()
    }

    private fun languagesToCharArray(languages: ArrayList<Language>): Array<out CharSequence> {
        val array = Array<CharSequence>(languages.size) {
            resources.getString(languages[it].stringResource)
        }

        return array
    }
}