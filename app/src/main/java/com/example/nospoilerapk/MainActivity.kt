package com.example.nospoilerapk

import android.os.Bundle
import com.example.nospoilerapk.ui.BaseActivity
import com.example.nospoilerapk.ui.MainScreen
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.nospoilerapk.ui.theme.NoSpoilerApkTheme
import androidx.activity.viewModels
import com.example.nospoilerapk.ui.viewmodels.ThemeViewModel
import com.example.nospoilerapk.ui.viewmodels.TermsViewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.rememberNavController
import com.example.nospoilerapk.ui.screens.TermsScreen
import androidx.compose.runtime.Composable

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val termsViewModel: TermsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(themeViewModel, termsViewModel)
        }
    }
}

@Composable
private fun MainContent(
    themeViewModel: ThemeViewModel,
    termsViewModel: TermsViewModel
) {
    val termsAccepted = termsViewModel.termsAccepted.collectAsState().value
    
    NoSpoilerApkTheme(themeViewModel = themeViewModel) {
        if (termsAccepted == false) {
            val navController = rememberNavController()
            TermsScreen(
                navController = navController,
                showBackButton = false
            )
        } else {
            MainScreen()
        }
    }
}