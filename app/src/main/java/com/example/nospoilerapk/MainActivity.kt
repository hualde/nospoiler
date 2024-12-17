package com.example.nospoilerapk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.nospoilerapk.ui.MainScreen
import com.example.nospoilerapk.ui.theme.NoSpoilerApkTheme
import com.example.nospoilerapk.ui.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoSpoilerApkTheme(themeViewModel = themeViewModel) {
                MainScreen()
            }
        }
    }
}