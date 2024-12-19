package com.example.nospoilerapk.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nospoilerapk.data.network.*
import com.example.nospoilerapk.data.model.MediaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RangeSelectorViewModel @Inject constructor(
    private val omdbService: OmdbService,
    private val perplexityService: PerplexityService
) : ViewModel() {

    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Loading)
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    fun loadMediaDetails(imdbId: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            try {
                val omdbResponse = async { omdbService.getMediaDetails(imdbId = imdbId) }
                
                if (omdbResponse.await().Response == "True") {
                    val media = omdbResponse.await()
                    
                    val perplexityRequest = PerplexityRequest(
                        messages = listOf(
                            Message(
                                role = "system",
                                content = "You are a helpful assistant that provides information about movies and TV shows. Respond only with the requested JSON format, no additional text."
                            ),
                            Message(
                                role = "user",
                                content = when (media.Type.lowercase()) {
                                    "series" -> """
                                        For the TV show '${media.Title}', tell me exactly how many episodes are in each season. 
                                        Be precise and respond in this exact JSON format: 
                                        {
                                            "totalSeasons": number,
                                            "episodesPerSeason": {
                                                "1": number,
                                                "2": number,
                                                etc
                                            }
                                        }
                                        Include only confirmed and aired episodes.
                                        """
                                    else -> """
                                        For the movie '${media.Title}', tell me if it has any parts or sequels. 
                                        Respond in this exact JSON format:
                                        {
                                            "totalParts": number,
                                            "parts": [
                                                {
                                                    "number": 1,
                                                    "title": "exact movie title",
                                                    "year": "year"
                                                }
                                            ]
                                        }
                                        Include only released movies, no announced or upcoming ones.
                                        """
                                }
                            )
                        )
                    )
                    
                    val episodeInfo = try {
                        val response = perplexityService.getMediaInfo(perplexityRequest)
                            .choices.firstOrNull()?.message?.content
                        
                        println("Perplexity response: $response") // Debug log
                        
                        if (response != null) {
                            // Intentar parsear directamente, sin reintento
                            response
                        } else {
                            println("Perplexity response was null") // Debug log
                            null
                        }
                    } catch (e: Exception) {
                        println("Error getting Perplexity info: ${e.message}") // Debug log
                        e.printStackTrace()
                        null
                    }

                    val parsedInfo = episodeInfo?.let { 
                        try {
                            MediaInfo.fromJson(it, media.Type).also { info ->
                                println("Parsed info: $info") // Debug log
                            }
                        } catch (e: Exception) {
                            println("Error parsing JSON: ${e.message}") // Debug log
                            e.printStackTrace()
                            null
                        }
                    }

                    _mediaState.value = MediaState.Success(
                        media = media,
                        episodeInfo = episodeInfo,
                        parsedInfo = parsedInfo
                    )
                } else {
                    _mediaState.value = MediaState.Error(omdbResponse.await().Error ?: "No results found")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            404 -> "Movie or series not found"
                            401 -> "Invalid API key"
                            else -> "Error: ${e.message()}"
                        }
                    }
                    else -> e.message ?: "Unknown error occurred"
                }
                _mediaState.value = MediaState.Error(errorMessage)
            }
        }
    }

    sealed class MediaState {
        object Loading : MediaState()
        data class Success(
            val media: DetailedMediaItem,
            val episodeInfo: String?,
            val parsedInfo: MediaInfo? = null
        ) : MediaState()
        data class Error(val message: String) : MediaState()
    }
} 