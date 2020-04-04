package com.log3900.settings.language

import android.content.Context
import android.content.res.Resources
import com.google.gson.JsonObject
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.user.account.AccountRepository
import io.reactivex.Completable
import org.greenrobot.eventbus.EventBus
import java.lang.Exception
import java.util.*

class LanguageManager {
    enum class LANGUAGE {
        SYSTEM,
        ENGLISH,
        FRENCH
    }

    companion object {
        private val registeredContexes: HashMap<Context, Int> = HashMap()
        private val languages: ArrayList<Language> = arrayListOf(
            Language(LANGUAGE.SYSTEM.ordinal, "", R.string.language_system_title),
            Language(LANGUAGE.ENGLISH.ordinal, "en", R.string.language_english_title),
            Language(LANGUAGE.FRENCH.ordinal, "fr", R.string.language_french_title)
        )

        fun getAvailableLanguages(): ArrayList<Language> {
            return languages
        }

        fun getCurrentLanguage(): Language {
            return try {
                languages[AccountRepository.getInstance().getAccount().languageID]
            } catch(e: Exception) {
                languages[0]
            }
        }

        fun getCurrentLanguageCode(): String {
            if (getCurrentLanguage().languageCode == "") {
                if (MainApplication.instance.getContext().resources.configuration.locale.language == "en") {
                    return "EN"
                } else {
                    return "FR"
                }
            } else {
                return getCurrentLanguage().languageCode.toUpperCase()
            }
        }

        fun changeLanguage(language: Language): Completable {
            val currentAccount = AccountRepository.getInstance().getAccount()
            currentAccount.languageID = language.index
            val currentLanguageCode = getCurrentLanguageCode()
            val dataObject = JsonObject()
            if (currentLanguageCode == "EN") {
                dataObject.addProperty("Language", 0)
            } else {
                dataObject.addProperty("Language", 1)
            }
            SocketService.instance?.sendJsonMessage(Event.LANGUAGE_CHANGED, dataObject.toString())

            return AccountRepository.getInstance().updateAccount(currentAccount)
        }

        fun applySavedLanguage(context: Context) {
            val res = context.resources
            val displayMetric = res.displayMetrics
            val configuration = res.configuration
            if (getCurrentLanguage().index != LANGUAGE.SYSTEM.ordinal) {
                configuration.setLocale(Locale(getCurrentLanguage().languageCode))
            } else {
                configuration.setLocale(Resources.getSystem().configuration.locales[0])
            }
            res.updateConfiguration(configuration, displayMetric)
            registeredContexes[context] = getCurrentLanguage().index
        }

        fun hasContextLanguageChanged(context: Context): Boolean {
            return registeredContexes[context] != getCurrentLanguage().index
        }
    }
}