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
import kotlin.math.roundToInt
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.nospoilerapk.navigation.Screen
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import com.example.nospoilerapk.R
import com.example.nospoilerapk.data.model.RangeSelectionMode
import android.util.Log
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSelectorScreen(
    navController: NavController,
    mediaId: String,
    viewModel: RangeSelectorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // Forzar recomposición cuando cambia la configuración
    DisposableEffect(configuration) {
        onDispose { }
    }

    val mediaState by viewModel.mediaState.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val startRange by viewModel.startRange.collectAsState()
    val endRange by viewModel.endRange.collectAsState()

    LaunchedEffect(mediaId) {
        if (mediaId.isNotBlank()) {
            viewModel.loadMediaDetails(mediaId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_range)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = mediaState) {
                is RangeSelectorViewModel.MediaState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is RangeSelectorViewModel.MediaState.Success -> {
                    val isFromBeginning by viewModel.fromBeginning.collectAsState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
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
                                                text = stringResource(R.string.total_seasons_format, state.media.totalSeasons),
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
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = when (state.media.Type.lowercase()) {
                                        "series" -> stringResource(R.string.select_episode_range)
                                        else -> stringResource(R.string.select_part_range)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))

                                when (val info = state.parsedInfo) {
                                    is MediaInfo.SeriesInfo -> {
                                        SummaryHeader(
                                            rangeStart = startRange,
                                            rangeEnd = endRange,
                                            season = selectedSeason,
                                            info = info,
                                            viewModel = viewModel
                                        )
                                    }
                                    is MediaInfo.MovieInfo -> {
                                        // Para películas
                                        Column {
                                            Text(stringResource(R.string.part_range_text, startRange, endRange))
                                            
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                            ) {
                                                Text(
                                                    text = "Rango de partes: $startRange - $endRange",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                RangeSlider(
                                                    value = startRange.toFloat()..endRange.toFloat(),
                                                    onValueChange = { range ->
                                                        viewModel.setRange(range.start.toInt(), range.endInclusive.toInt())
                                                    },
                                                    valueRange = 1f..info.totalParts.toFloat(),
                                                    steps = info.totalParts - 2,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                    null -> {
                                        // Si no tenemos información de Perplexity, mostramos un mensaje y un botón para reintentar
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = stringResource(R.string.loading_episode_info),
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
                                                Text(stringResource(R.string.retry_button))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        navController.navigate(
                                            Screen.Summary.createRoute(
                                                mediaId = state.media.imdbID,
                                                rangeStart = startRange,
                                                rangeEnd = endRange,
                                                season = selectedSeason,
                                                isFromBeginning = isFromBeginning
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.get_summary))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { 
                                        navController.navigate(
                                            Screen.Timeline.createRoute(
                                                mediaId = state.media.imdbID,
                                                rangeStart = startRange,
                                                rangeEnd = endRange,
                                                season = selectedSeason,
                                                isFromBeginning = isFromBeginning
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.view_timeline))
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
                            Text(stringResource(R.string.go_back_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(
    rangeStart: Int,
    rangeEnd: Int,
    season: Int,
    info: MediaInfo.SeriesInfo,
    viewModel: RangeSelectorViewModel
) {
    val context = LocalContext.current
    val selectionMode by viewModel.selectionMode.collectAsState()

    Column {
        // Selector de modo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectionMode == RangeSelectionMode.NORMAL,
                onClick = { viewModel.setSelectionMode(RangeSelectionMode.NORMAL) },
                label = { Text(stringResource(R.string.mode_normal)) }
            )
            FilterChip(
                selected = selectionMode == RangeSelectionMode.FROM_BEGINNING,
                onClick = { viewModel.setSelectionMode(RangeSelectionMode.FROM_BEGINNING) },
                label = { Text(stringResource(R.string.from_beginning)) }
            )
            FilterChip(
                selected = selectionMode == RangeSelectionMode.COMPLETE_SEASON,
                onClick = { viewModel.setSelectionMode(RangeSelectionMode.COMPLETE_SEASON) },
                label = { Text(stringResource(R.string.complete_season)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de temporada y episodios
        SeasonSelector(
            info = info,
            selectedSeason = season,
            startRange = rangeStart,
            endRange = rangeEnd,
            viewModel = viewModel,
            selectionMode = selectionMode
        )
    }
}

@Composable
private fun SeasonSelector(
    info: MediaInfo.SeriesInfo,
    selectedSeason: Int,
    startRange: Int,
    endRange: Int,
    viewModel: RangeSelectorViewModel,
    selectionMode: RangeSelectionMode
) {
    var expanded by remember { mutableStateOf(false) }
    val maxEpisodes = info.episodesPerSeason[selectedSeason.toString()] ?: 1

    // Cuando cambia la temporada o el modo, ajustamos el rango
    LaunchedEffect(selectedSeason, selectionMode) {
        when (selectionMode) {
            RangeSelectionMode.NORMAL -> {
                viewModel.setRange(1, maxEpisodes)
            }
            RangeSelectionMode.FROM_BEGINNING -> {
                viewModel.setRange(1, maxEpisodes)
            }
            RangeSelectionMode.COMPLETE_SEASON -> {
                viewModel.setRange(1, maxEpisodes)
            }
        }
    }

    Column {
        // Texto explicativo según el modo
        Text(
            text = when (selectionMode) {
                RangeSelectionMode.NORMAL -> stringResource(R.string.select_season_episode_range)
                RangeSelectionMode.FROM_BEGINNING -> stringResource(R.string.select_until_season_episode)
                RangeSelectionMode.COMPLETE_SEASON -> stringResource(R.string.select_complete_season)
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Selector de temporada
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
                Text(stringResource(R.string.season_with_episodes, selectedSeason, maxEpisodes))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.select_season_description)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                (1..info.totalSeasons).forEach { season ->
                    val episodesInSeason = info.episodesPerSeason[season.toString()] ?: 1
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.season_episodes_dropdown, season, episodesInSeason)) },
                        onClick = {
                            viewModel.setSelectedSeason(season)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Solo mostramos los sliders si no estamos en modo COMPLETE_SEASON
        if (selectionMode != RangeSelectionMode.COMPLETE_SEASON) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (selectionMode == RangeSelectionMode.FROM_BEGINNING) {
                    Text(
                        text = stringResource(R.string.until_episode_format, selectedSeason, endRange),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = endRange.toFloat(),
                        onValueChange = { value ->
                            viewModel.setRange(1, value.toInt())
                        },
                        valueRange = 1f..maxEpisodes.toFloat(),
                        steps = maxEpisodes - 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = stringResource(R.string.episode_range_format, startRange, endRange),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    RangeSlider(
                        value = startRange.toFloat()..endRange.toFloat(),
                        onValueChange = { range ->
                            viewModel.setRange(range.start.toInt(), range.endInclusive.toInt())
                        },
                        valueRange = 1f..maxEpisodes.toFloat(),
                        steps = maxEpisodes - 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
} 