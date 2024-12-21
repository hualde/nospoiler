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
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.stringResource
import com.example.nospoilerapk.R
import androidx.compose.foundation.clickable

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
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val currentState = state) {
            is SearchState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is SearchState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentState.results) { media ->
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
                                        text = stringResource(R.string.year_type_format, media.Year, media.Type),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is SearchState.Error -> {
                Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is SearchState.Initial -> {
                Text(
                    text = stringResource(R.string.enter_title),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
} 