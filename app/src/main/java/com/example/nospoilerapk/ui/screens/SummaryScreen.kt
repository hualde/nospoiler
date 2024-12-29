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

    when {
        state.isLoading -> LoadingContent()
        state.error != null -> ErrorContent(state.error!!)
        else -> SummaryContent(state)
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SummaryContent(state: SummaryScreenState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header con poster e información básica
        MediaHeader(state.mediaDetails)
        
        // Información del rango de episodios
        EpisodeRangeInfo(
            season = state.season,
            rangeStart = state.rangeStart,
            rangeEnd = state.rangeEnd,
            totalEpisodes = state.mediaDetails?.Episodes?.toIntOrNull() ?: 0
        )
        
        // Resumen
        SummaryCard(state.summary)
        
        // Información adicional
        AdditionalInfo(state.mediaDetails)
    }
}

@Composable
private fun MediaHeader(mediaDetails: DetailedMediaItem?) {
    if (mediaDetails == null) return
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Fondo con el poster
        AsyncImage(
            model = mediaDetails.Poster,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 8.dp),
            contentScale = ContentScale.Crop
        )
        
        // Overlay oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        
        // Contenido del header
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Poster
            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = mediaDetails.Poster,
                    contentDescription = null,
                    modifier = Modifier.size(width = 120.dp, height = 180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mediaDetails.Title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = mediaDetails.Year,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "IMDb: ${mediaDetails.imdbRating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = mediaDetails.Genre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
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
                text = "Temporada $season",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = if (totalEpisodes > 0) {
                    "Episodios $rangeStart a $rangeEnd de $totalEpisodes"
                } else {
                    "Episodios $rangeStart a $rangeEnd"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AdditionalInfo(mediaDetails: DetailedMediaItem?) {
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
                text = "Información Adicional",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Director: ${mediaDetails.Director}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Actores: ${mediaDetails.Actors}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (mediaDetails.Awards.isNotBlank()) {
                Text(
                    text = "Premios: ${mediaDetails.Awards}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}