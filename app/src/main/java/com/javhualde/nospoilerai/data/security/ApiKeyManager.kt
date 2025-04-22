package com.javhualde.nospoilerapk.data.security

import android.content.Context
import android.util.Log
import java.util.Properties

class ApiKeyManager(private val context: Context) {
    private val properties = Properties()

    init {
        try {
            context.assets.open("api_keys.properties").use {
                properties.load(it)
            }
        } catch (e: Exception) {
            Log.e("ApiKeyManager", "Error loading API keys", e)
        }
    }

    val perplexityApiKey: String
        get() = properties.getProperty("PERPLEXITY_API_KEY", "")

    val omdbApiKey: String
        get() = properties.getProperty("OMDB_API_KEY", "")
} 