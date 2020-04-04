package com.log3900.settings

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

object LocaleLanguageHelper {
    fun getLocalizedResources(context: Context, localeString: String): Resources {
        val configuration = getLocalizedConfiguration(context, localeString)
        return context.createConfigurationContext(configuration).resources
    }

    private fun getLocalizedConfiguration(context: Context, localeString: String): Configuration {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(localeString))
        return configuration
    }
}