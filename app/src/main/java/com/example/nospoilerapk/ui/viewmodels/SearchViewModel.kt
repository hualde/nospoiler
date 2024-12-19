package com.example.nospoilerapk.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nospoilerapk.data.network.MediaItem
import com.example.nospoilerapk.data.network.OmdbService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val omdbService: OmdbService
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun searchMedia(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val response = omdbService.searchMedia(searchQuery = query)
                if (response.Response == "True") {
                    _searchState.value = SearchState.Success(response.Search ?: emptyList())
                } else {
                    _searchState.value = SearchState.Error(response.Error ?: "No results found")
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<MediaItem>) : SearchState()
    data class Error(val message: String) : SearchState()
} 