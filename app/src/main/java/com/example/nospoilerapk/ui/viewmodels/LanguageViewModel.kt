package com.example.nospoilerapk.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _selectedLanguage = MutableStateFlow(
        prefs.getString("selected_language", "English") ?: "English"
    )
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    fun setLanguage(language: String, activity: Activity?) {
        viewModelScope.launch {
            prefs.edit()
                .putString("selected_language", language)
                .apply()
            _selectedLanguage.value = language
            
            activity?.let {
                val intent = it.packageManager.getLaunchIntentForPackage(it.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.startActivity(intent)
                it.finish()
            }
        }
    }
} 