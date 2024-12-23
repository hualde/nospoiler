package com.example.nospoilerapk.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _termsAccepted = MutableStateFlow<Boolean?>(null)
    val termsAccepted: StateFlow<Boolean?> = _termsAccepted

    init {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            _termsAccepted.value = prefs.getBoolean("terms_accepted", false)
        }
    }

    fun acceptTerms() {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("terms_accepted", true)
            .apply()
        _termsAccepted.value = true
    }

    fun declineTerms() {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("terms_accepted", false)
            .apply()
        _termsAccepted.value = false
    }
} 