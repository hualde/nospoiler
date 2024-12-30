package com.example.nospoilerapk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nospoilerapk.ui.viewmodels.ThemeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nospoilerapk.ui.viewmodels.LanguageViewModel
import androidx.compose.ui.res.stringResource
import com.example.nospoilerapk.R
import android.app.Activity
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.nospoilerapk.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val selectedLanguage by languageViewModel.selectedLanguage.collectAsState()
    
    val languages = listOf("English", "Español", "Français", "Deutsch")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Dark Mode Switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(R.string.dark_mode))
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { themeViewModel.toggleTheme() }
                    )
                }
            }

            // Language Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = { showLanguageDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(R.string.language))
                    }
                    Text(selectedLanguage)
                }
            }

            // About Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = { navController.navigate(Screen.About.route) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.about))
                }
            }

            // Help Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = { navController.navigate(Screen.Help.route) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.help))
                }
            }

            // Terms Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = { navController.navigate(Screen.Terms.route) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.terms_and_conditions))
                }
            }

            // Privacy Policy Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = { navController.navigate(Screen.Privacy.route) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.privacy_policy))
                }
            }
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(stringResource(R.string.select_language)) },
                text = {
                    Column {
                        languages.forEach { language ->
                            TextButton(
                                onClick = {
                                    val activity = context as? Activity
                                    languageViewModel.setLanguage(language, activity)
                                    showLanguageDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(language)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
} 