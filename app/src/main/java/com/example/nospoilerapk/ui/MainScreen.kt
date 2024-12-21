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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.nospoilerapk.navigation.Screen
import com.example.nospoilerapk.ui.screens.*
import androidx.compose.ui.res.stringResource
import com.example.nospoilerapk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = stringResource(R.string.settings)
                        )
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
            composable(
                route = Screen.RangeSelector.route,
                arguments = listOf(
                    navArgument(Screen.RangeSelector.MEDIA_ID) { 
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val mediaId = requireNotNull(backStackEntry.arguments?.getString(Screen.RangeSelector.MEDIA_ID))
                RangeSelectorScreen(
                    navController = navController,
                    mediaId = mediaId
                )
            }
            composable(Screen.SearchResults.route) { 
                SearchResultsScreen(navController)
            }
            composable(
                route = Screen.Summary.route,
                arguments = listOf(
                    navArgument(Screen.Summary.MEDIA_ID) { type = NavType.StringType },
                    navArgument(Screen.Summary.RANGE_START) { type = NavType.IntType },
                    navArgument(Screen.Summary.RANGE_END) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                SummaryScreen(
                    navController = navController,
                    mediaId = backStackEntry.arguments?.getString(Screen.Summary.MEDIA_ID) ?: "",
                    rangeStart = backStackEntry.arguments?.getInt(Screen.Summary.RANGE_START) ?: 1,
                    rangeEnd = backStackEntry.arguments?.getInt(Screen.Summary.RANGE_END) ?: 1
                )
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(navController)
            }
            composable(
                route = Screen.Timeline.route,
                arguments = listOf(
                    navArgument(Screen.Timeline.MEDIA_ID) { type = NavType.StringType },
                    navArgument(Screen.Timeline.RANGE_START) { type = NavType.IntType },
                    navArgument(Screen.Timeline.RANGE_END) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                TimelineScreen(
                    navController = navController,
                    mediaId = backStackEntry.arguments?.getString(Screen.Timeline.MEDIA_ID) ?: "",
                    rangeStart = backStackEntry.arguments?.getInt(Screen.Timeline.RANGE_START) ?: 1,
                    rangeEnd = backStackEntry.arguments?.getInt(Screen.Timeline.RANGE_END) ?: 1
                )
            }
        }
    }
} 