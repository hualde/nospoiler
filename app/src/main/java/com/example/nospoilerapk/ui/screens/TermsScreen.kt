package com.example.nospoilerapk.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nospoilerapk.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nospoilerapk.ui.viewmodels.TermsViewModel
import android.widget.Toast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    navController: NavController,
    showBackButton: Boolean = true,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.terms_and_conditions)) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.go_back)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Terms content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = stringResource(R.string.terms_content),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.acceptTerms()
                            if (showBackButton) {
                                navController.navigateUp()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.accept))
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            viewModel.declineTerms()
                            if (!showBackButton) {
                                (context as? Activity)?.finishAffinity()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.terms_decline_message),
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.decline))
                }
            }
        }
    }
} 