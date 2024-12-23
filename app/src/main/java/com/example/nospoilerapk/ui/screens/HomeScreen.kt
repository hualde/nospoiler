package com.example.nospoilerapk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nospoilerapk.navigation.Screen
import com.example.nospoilerapk.ui.viewmodels.SearchState
import com.example.nospoilerapk.ui.viewmodels.SearchViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.stringResource
import com.example.nospoilerapk.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.nospoilerapk.data.network.MediaItem

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val state by viewModel.searchState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sección de búsqueda
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.welcome_message),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchMedia(searchQuery) }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_button)
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.searchMedia(searchQuery) }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Estado de la búsqueda
        when (val currentState = state) {
            is SearchState.Loading -> {
                LoadingIndicator()
            }
            is SearchState.Success -> {
                SearchResults(currentState.results, navController)
            }
            is SearchState.Error -> {
                ErrorMessage(currentState.message)
            }
            is SearchState.Initial -> {
                InitialContent(navController)
            }
        }
    }
}

@Composable
private fun InitialContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de características principales
        FeaturesSection()
        
        // Sección de accesos rápidos
        QuickAccessSection(navController)
        
        // Sección de ayuda rápida
        QuickHelpSection(navController)
    }
}

@Composable
private fun FeaturesSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.main_features),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FeatureItem(
                icon = Icons.Default.Movie,
                text = stringResource(R.string.feature_movies_series)
            )
            FeatureItem(
                icon = Icons.Default.Timeline,
                text = stringResource(R.string.feature_timeline)
            )
            FeatureItem(
                icon = Icons.Default.Description,
                text = stringResource(R.string.feature_summaries)
            )
        }
    }
}

@Composable
private fun QuickAccessSection(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickAccessCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Help,
            text = stringResource(R.string.help),
            onClick = { navController.navigate(Screen.Help.route) }
        )
        QuickAccessCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Settings,
            text = stringResource(R.string.settings),
            onClick = { navController.navigate(Screen.Settings.route) }
        )
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SearchResults(results: List<MediaItem>, navController: NavController) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(results) { media ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(
                            Screen.RangeSelector.createRoute(media.imdbID)
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = media.Poster,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Column {
                        Text(
                            text = media.Title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.year_type_format,
                                media.Year,
                                media.Type
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun QuickHelpSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_help_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.faq_search_content),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 