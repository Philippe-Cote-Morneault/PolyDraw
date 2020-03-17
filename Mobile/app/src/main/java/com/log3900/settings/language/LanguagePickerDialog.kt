package com.log3900.settings.language

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.log3900.MainApplication
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import org.greenrobot.eventbus.EventBus

class LanguagePickerDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var selectedLanguage = LanguageManager.getCurrentLanguage()
        val dialogBuilder = AlertDialog.Builder(activity)
            .setTitle("Language Picker")
            .setPositiveButton("Save") { _, _ ->
                LanguageManager.changeLanguage(selectedLanguage).subscribe {
                    LanguageManager.applySavedLanguage(MainApplication.instance.baseContext)
                    EventBus.getDefault().post(MessageEvent(EventType.LANGUAGE_CHANGED, null))
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