package com.example.nospoilerapk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nospoilerapk.ui.viewmodels.SummaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController,
    mediaId: String,
    rangeStart: Int,
    rangeEnd: Int,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(mediaId, rangeStart, rangeEnd) {
        viewModel.getSummary(mediaId, rangeStart, rangeEnd)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = summaryState) {
                is SummaryViewModel.SummaryState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SummaryViewModel.SummaryState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Episodes $rangeStart to $rangeEnd",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = state.summary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Justify,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
                                )
                            }
                        }
                    }
                }
                is SummaryViewModel.SummaryState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.getSummary(mediaId, rangeStart, rangeEnd)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
} 