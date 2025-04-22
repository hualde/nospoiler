package com.javhualde.nospoilerapk.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.javhualde.nospoilerapk.navigation.Screen
import com.javhualde.nospoilerapk.ui.screens.*
import androidx.compose.ui.res.stringResource
import com.javhualde.nospoilerapk.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        buildAnnotatedString {
                            append("NoSpoiler")
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF0D47A1)
                                )
                            ) {
                                append("AI")
                            }
                        }
                    )
                },
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
                HomeScreen(navController = navController)
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
                    navArgument(Screen.Summary.RANGE_END) { type = NavType.IntType },
                    navArgument(Screen.Summary.SEASON) { type = NavType.IntType },
                    navArgument(Screen.Summary.IS_FROM_BEGINNING) { 
                        type = NavType.BoolType 
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getString(Screen.Summary.MEDIA_ID) ?: ""
                val rangeStart = backStackEntry.arguments?.getInt(Screen.Summary.RANGE_START) ?: 1
                val rangeEnd = backStackEntry.arguments?.getInt(Screen.Summary.RANGE_END) ?: 1
                val season = backStackEntry.arguments?.getInt(Screen.Summary.SEASON) ?: 1
                val isFromBeginning = backStackEntry.arguments?.getBoolean(Screen.Summary.IS_FROM_BEGINNING) ?: false
                
                SummaryScreen(
                    navController = navController,
                    mediaId = mediaId,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    season = season,
                    isFromBeginning = isFromBeginning
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
                    navArgument(Screen.Timeline.RANGE_END) { type = NavType.IntType },
                    navArgument(Screen.Timeline.SEASON) { type = NavType.IntType },
                    navArgument(Screen.Timeline.IS_FROM_BEGINNING) { 
                        type = NavType.BoolType 
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getString(Screen.Timeline.MEDIA_ID) ?: ""
                val rangeStart = backStackEntry.arguments?.getInt(Screen.Timeline.RANGE_START) ?: 1
                val rangeEnd = backStackEntry.arguments?.getInt(Screen.Timeline.RANGE_END) ?: 1
                val season = backStackEntry.arguments?.getInt(Screen.Timeline.SEASON) ?: 1
                val isFromBeginning = backStackEntry.arguments?.getBoolean(Screen.Timeline.IS_FROM_BEGINNING) ?: false
                
                TimelineScreen(
                    navController = navController,
                    mediaId = mediaId,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    season = season,
                    isFromBeginning = isFromBeginning
                )
            }
            composable(Screen.About.route) {
                AboutScreen(navController)
            }
            composable(Screen.Help.route) {
                HelpScreen(navController)
            }
            composable(Screen.Terms.route) {
                TermsScreen(
                    navController = navController,
                    showBackButton = true
                )
            }
            composable(Screen.Privacy.route) {
                PrivacyPolicyScreen(navController)
            }
        }
    }
} 