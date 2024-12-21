package com.example.nospoilerapk.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.nospoilerapk.data.LanguageService

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageService: LanguageService
) : ViewModel() {
    private val _selectedLanguage = MutableStateFlow(languageService.getCurrentLanguage())
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    fun setLanguage(language: String, activity: Activity?) {
        viewModelScope.launch {
            languageService.setLanguage(language)
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