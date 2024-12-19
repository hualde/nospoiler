package com.example.nospoilerapk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nospoilerapk.ui.viewmodels.RangeSelectorViewModel
import com.example.nospoilerapk.data.model.MediaInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSelectorScreen(
    navController: NavController,
    mediaId: String,
    viewModel: RangeSelectorViewModel = hiltViewModel()
) {
    val mediaState by viewModel.mediaState.collectAsState()
    var startRange by remember { mutableStateOf(1) }
    var endRange by remember { mutableStateOf(1) }

    LaunchedEffect(mediaId) {
        if (mediaId.isNotBlank()) {
            try {
                viewModel.loadMediaDetails(mediaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Range") },
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
            when (val state = mediaState) {
                is RangeSelectorViewModel.MediaState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is RangeSelectorViewModel.MediaState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AsyncImage(
                                        model = state.media.Poster,
                                        contentDescription = null,
                                        modifier = Modifier.size(120.dp)
                                    )
                                    Column {
                                        Text(
                                            text = state.media.Title,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.media.Year,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.media.Genre,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (state.media.totalSeasons != null) {
                                            Text(
                                                text = "Total Seasons: ${state.media.totalSeasons}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = state.media.Plot,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = when (state.media.Type.lowercase()) {
                                        "series" -> "Select episode range:"
                                        else -> "Select part range:"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                when (val info = state.parsedInfo) {
                                    is MediaInfo.SeriesInfo -> {
                                        // Para series
                                        var selectedSeason by remember { mutableStateOf(1) }
                                        var expanded by remember { mutableStateOf(false) }
                                        val maxEpisodes = info.episodesPerSeason[selectedSeason.toString()] ?: 1
                                        
                                        // Ajustar los rangos cuando cambie la temporada
                                        LaunchedEffect(selectedSeason) {
                                            startRange = 1
                                            endRange = maxEpisodes
                                        }
                                        
                                        Column {
                                            // Selector de temporada con dropdown
                                            OutlinedCard(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { expanded = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Season $selectedSeason (${maxEpisodes} episodes)")
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = "Select season"
                                                    )
                                                }
                                                
                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    (1..info.totalSeasons).forEach { season ->
                                                        val episodesInSeason = info.episodesPerSeason[season.toString()] ?: 1
                                                        DropdownMenuItem(
                                                            text = { Text("Season $season ($episodesInSeason episodes)") },
                                                            onClick = {
                                                                selectedSeason = season
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Rango de episodios con sliders
                                            Text("Episode Range: $startRange - $endRange of $maxEpisodes")
                                            
                                            RangeSlider(
                                                value = startRange.toFloat()..endRange.toFloat(),
                                                onValueChange = { range ->
                                                    startRange = range.start.toInt()
                                                    endRange = range.endInclusive.toInt()
                                                },
                                                valueRange = 1f..maxEpisodes.toFloat(),
                                                steps = maxEpisodes - 1
                                            )
                                        }
                                    }
                                    is MediaInfo.MovieInfo -> {
                                        // Para películas
                                        Column {
                                            Text("Part Range: $startRange - $endRange")
                                            
                                            RangeSlider(
                                                value = startRange.toFloat()..endRange.toFloat(),
                                                onValueChange = { range ->
                                                    startRange = range.start.toInt()
                                                    endRange = range.endInclusive.toInt()
                                                },
                                                valueRange = 1f..info.totalParts.toFloat(),
                                                steps = info.totalParts - 1
                                            )
                                        }
                                    }
                                    null -> {
                                        // Si no tenemos información de Perplexity, mostramos un mensaje y un botón para reintentar
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Loading episode information...",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            CircularProgressIndicator()
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Button(
                                                onClick = { 
                                                    viewModel.loadMediaDetails(mediaId)
                                                }
                                            ) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        navController.navigate(
                                            "summary/${state.media.imdbID}/$startRange/$endRange"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Generate Summary")
                                }
                            }
                        }
                    }
                }
                is RangeSelectorViewModel.MediaState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
} 