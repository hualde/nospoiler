package com.example.nospoilerapk

import android.os.Bundle
import com.example.nospoilerapk.ui.BaseActivity
import com.example.nospoilerapk.ui.MainScreen
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.nospoilerapk.ui.theme.NoSpoilerApkTheme
import androidx.activity.viewModels
import com.example.nospoilerapk.ui.viewmodels.ThemeViewModel

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoSpoilerApkTheme(themeViewModel = themeViewModel) {
                MainScreen()
            }
        }
    }
}