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
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search movies or TV shows") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = { 
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchMedia(searchQuery)
                        }
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = searchState) {
            is SearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SearchState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.results) { media ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navController.navigate(Screen.RangeSelector.createRoute(media.imdbID))
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
                                        text = "${media.Year} - ${media.Type}",
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
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is SearchState.Initial -> {
                Text(
                    text = "Enter a title and press the search button",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
} 