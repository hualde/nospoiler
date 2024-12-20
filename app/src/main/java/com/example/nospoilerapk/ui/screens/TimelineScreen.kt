package com.example.nospoilerapk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nospoilerapk.ui.viewmodels.TimelineViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    navController: NavController,
    mediaId: String,
    rangeStart: Int,
    rangeEnd: Int,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val timelineState by viewModel.timelineState.collectAsState()

    LaunchedEffect(mediaId, rangeStart, rangeEnd) {
        viewModel.getTimeline(mediaId, rangeStart, rangeEnd)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = timelineState) {
                is TimelineViewModel.TimelineState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TimelineViewModel.TimelineState.Success -> {
                    TimelineList(
                        events = state.events,
                        rangeStart = rangeStart,
                        rangeEnd = rangeEnd
                    )
                }
                is TimelineViewModel.TimelineState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.getTimeline(mediaId, rangeStart, rangeEnd) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineList(
    events: List<String>,
    rangeStart: Int,
    rangeEnd: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TimelineHeader(rangeStart = rangeStart, rangeEnd = rangeEnd)
        }

        itemsIndexed(events) { index, event ->
            TimelineEventItem(
                event = event,
                index = index,
                isLast = index == events.lastIndex
            )
        }
    }
}

@Composable
private fun TimelineHeader(rangeStart: Int, rangeEnd: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "Episodes $rangeStart to $rangeEnd",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun TimelineEventItem(
    event: String,
    index: Int,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "${index + 1}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = event,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
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