package com.example.nospoilerapk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import android.util.Log
import com.example.nospoilerapk.R
import com.example.nospoilerapk.data.network.DetailedMediaItem
import com.example.nospoilerapk.ui.viewmodels.SummaryViewModel
import com.example.nospoilerapk.ui.viewmodels.SummaryViewModel.SummaryScreenState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.awaitAll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import com.example.nospoilerapk.ui.components.MediaHeader
import com.example.nospoilerapk.ui.components.AnimatedLoadingProgress

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = hiltViewModel(),
    mediaId: String,
    season: Int,
    rangeStart: Int,
    rangeEnd: Int,
    isFromBeginning: Boolean
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(mediaId, season, rangeStart, rangeEnd) {
        viewModel.loadContent(mediaId, season, rangeStart, rangeEnd, isFromBeginning)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header con poster e información básica - siempre visible
            MediaHeader(state.mediaDetails)
            
            // Información del rango de episodios - siempre visible si hay detalles
            if (state.mediaDetails != null) {
                EpisodeRangeInfo(
                    season = state.season,
                    rangeStart = state.rangeStart,
                    rangeEnd = state.rangeEnd,
                    totalEpisodes = state.mediaDetails?.Episodes?.toIntOrNull() ?: 0
                )
            }
            
            // Contenido que depende del estado de carga
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedLoadingProgress(
                            isTimeline = false,
                            isComplete = state.summary.isNotEmpty()
                        )
                    }
                }
                state.error != null -> ErrorContent(state.error!!)
                else -> {
                    SummaryCard(state.summary)
                    AdditionalInfo(
                        mediaDetails = state.mediaDetails,
                        actorImages = state.actorImages
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Justify,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
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
    }
}

@Composable
private fun EpisodeRangeInfo(
    season: Int,
    rangeStart: Int,
    rangeEnd: Int,
    totalEpisodes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.season_format, season),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = if (totalEpisodes > 0) {
                    stringResource(R.string.episodes_range_with_total, rangeStart, rangeEnd, totalEpisodes)
                } else {
                    stringResource(R.string.episodes_range, rangeStart, rangeEnd)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AdditionalInfo(
    mediaDetails: DetailedMediaItem?,
    actorImages: Map<String, String> = emptyMap()
) {
    if (mediaDetails == null) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.additional_info),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.director_format, mediaDetails.Director),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = stringResource(R.string.actors_label),
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Sección de Actores con imágenes
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(mediaDetails.Actors.split(",").map { it.trim() }) { actorName ->
                    ActorCard(
                        name = actorName,
                        imageUrl = actorImages[actorName]
                    )
                }
            }
            
            if (mediaDetails.Awards.isNotBlank()) {
                Text(
                    text = stringResource(R.string.awards_format, mediaDetails.Awards),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ActorCard(
    name: String,
    imageUrl: String?
) {
    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.actor_photo_description, name),
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder cuando no hay imagen
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.actor_placeholder_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}