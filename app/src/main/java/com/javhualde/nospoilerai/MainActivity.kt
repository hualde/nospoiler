package com.javhualde.nospoilerapk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.javhualde.nospoilerapk.ui.BaseActivity
import com.javhualde.nospoilerapk.ui.MainScreen
import com.javhualde.nospoilerapk.ui.screens.PrivacyPolicyScreen
import com.javhualde.nospoilerapk.ui.screens.SplashScreen
import com.javhualde.nospoilerapk.ui.screens.TermsScreen
import com.javhualde.nospoilerapk.ui.theme.NoSpoilerApkTheme
import com.javhualde.nospoilerapk.ui.viewmodels.TermsViewModel
import com.javhualde.nospoilerapk.ui.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val termsViewModel: TermsViewModel by viewModels()
    private var showSplash by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (showSplash) {
                SplashScreen(onSplashFinished = { showSplash = false })
            } else {
                MainContent(themeViewModel, termsViewModel)
            }
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
            InitialTermsAndPrivacy()
        } else {
            MainScreen()
        }
    }
}

@Composable
private fun InitialTermsAndPrivacy() {
    val navController = rememberNavController()
    Column {
        TermsScreen(
            navController = navController,
            showBackButton = false
        )
        PrivacyPolicyScreen(
            navController = navController
        )
    }
}