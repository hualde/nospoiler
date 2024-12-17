package com.example.nospoilerapk.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nospoilerapk.navigation.Screen
import com.example.nospoilerapk.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NoSpoiler") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(navController)
            }
            composable(Screen.SearchResults.route) { 
                SearchResultsScreen(navController)
            }
            composable(Screen.RangeSelector.route) { 
                RangeSelectorScreen(navController)
            }
            composable(Screen.Summary.route) { 
                SummaryScreen(navController)
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(navController)
            }
        }
    }
} 