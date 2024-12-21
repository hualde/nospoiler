package com.example.nospoilerapk

import android.os.Bundle
import com.example.nospoilerapk.ui.BaseActivity
import com.example.nospoilerapk.ui.MainScreen
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}