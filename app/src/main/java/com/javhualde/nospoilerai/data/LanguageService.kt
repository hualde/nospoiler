package com.javhualde.nospoilerapk.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getCurrentLanguage(): String {
        return prefs.getString("selected_language", "English") ?: "English"
    }

    fun getCurrentLanguageCode(): String {
        return when (getCurrentLanguage()) {
            "Español" -> "es"
            "Français" -> "fr"
            "Deutsch" -> "de"
            else -> "en"
        }
    }

    fun setLanguage(language: String) {
        prefs.edit()
            .putString("selected_language", language)
            .apply()
    }
} 