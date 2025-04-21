package com.javhualde.nospoilerapk.ui.screens

import androidx.compose.foundation.layout.*
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
import com.javhualde.nospoilerapk.ui.viewmodels.TimelineViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.javhualde.nospoilerapk.ui.components.MediaHeader
import com.javhualde.nospoilerapk.ui.components.LoadingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    navController: NavController,
    mediaId: String,
    rangeStart: Int,
    rangeEnd: Int,
    season: Int,
    isFromBeginning: Boolean = false,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val timelineState by viewModel.timelineState.collectAsState()
    val mediaDetails by viewModel.mediaDetails.collectAsState()

    LaunchedEffect(mediaId, rangeStart, rangeEnd, season) {
        viewModel.getTimeline(mediaId, rangeStart, rangeEnd, season, isFromBeginning)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timeline") },
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
        ) {
            // Mostrar el MediaHeader
            MediaHeader(mediaDetails = mediaDetails)
            
            // Contenido de la timeline
            Box(modifier = Modifier.weight(1f)) {
                when (val state = timelineState) {
                    is TimelineViewModel.TimelineState.Loading -> {
                        LoadingDialog(isTimeline = true)
                    }
                    is TimelineViewModel.TimelineState.Success -> {
                        TimelineContent(state.events)
                    }
                    is TimelineViewModel.TimelineState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.getTimeline(mediaId, rangeStart, rangeEnd, season, isFromBeginning) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineContent(events: Map<String, List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        events.forEach { (title, sectionEvents) ->
            TimelineSection(title, sectionEvents)
        }
    }
}

@Composable
private fun TimelineSection(title: String, events: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        events.forEach { event ->
            TimelineEvent(event)
        }
    }
}

@Composable
private fun TimelineEvent(event: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = event,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
} 