package com.example.nospoilerapk.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val languageCode = when (prefs.getString("selected_language", "English")) {
            "Español" -> "es"
            "Français" -> "fr"
            "Deutsch" -> "de"
            else -> "en"
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = newBase.resources.configuration
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        
        super.attachBaseContext(context)
    }
} 