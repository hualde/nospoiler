package com.example.nospoilerapk.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nospoilerapk.data.network.*
import com.example.nospoilerapk.data.model.MediaInfo
import com.example.nospoilerapk.data.model.RangeSelectionMode
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
    val mediaState: StateFlow<MediaState> = _mediaState

    private val _selectedSeason = MutableStateFlow(1)
    val selectedSeason: StateFlow<Int> = _selectedSeason

    private val _startRange = MutableStateFlow(1)
    val startRange: StateFlow<Int> = _startRange

    private val _endRange = MutableStateFlow(1)
    val endRange: StateFlow<Int> = _endRange

    private val _selectionMode = MutableStateFlow(RangeSelectionMode.NORMAL)
    val selectionMode: StateFlow<RangeSelectionMode> = _selectionMode

    private val _fullSeriesEndSeason = MutableStateFlow(1)
    val fullSeriesEndSeason: StateFlow<Int> = _fullSeriesEndSeason.asStateFlow()

    private val _fullSeriesEndEpisode = MutableStateFlow(1)
    val fullSeriesEndEpisode: StateFlow<Int> = _fullSeriesEndEpisode.asStateFlow()

    private var cachedMediaDetails: DetailedMediaItem? = null
    private var cachedParsedInfo: MediaInfo? = null

    private val _fromBeginning = MutableStateFlow(false)
    val fromBeginning: StateFlow<Boolean> = _fromBeginning.asStateFlow()

    fun setSelectedSeason(season: Int) {
        _selectedSeason.value = season
    }

    fun setRange(start: Int, end: Int) {
        _startRange.value = start
        _endRange.value = end
    }

    fun setSelectionMode(mode: RangeSelectionMode) {
        _selectionMode.value = mode
        // Ajustar el rango según el modo seleccionado
        when (mode) {
            RangeSelectionMode.COMPLETE_SEASON -> {
                val maxEpisodes = (mediaState.value as? MediaState.Success)?.let { state ->
                    (state.parsedInfo as? MediaInfo.SeriesInfo)?.episodesPerSeason?.get(_selectedSeason.value.toString())
                } ?: 1
                setRange(1, maxEpisodes)
            }
            RangeSelectionMode.FROM_BEGINNING -> {
                // Mantener el comportamiento actual
            }
            RangeSelectionMode.NORMAL -> {
                // Mantener el comportamiento actual
            }
        }
    }

    fun setFullSeriesRange(endSeason: Int, endEpisode: Int) {
        _fullSeriesEndSeason.value = endSeason
        _fullSeriesEndEpisode.value = endEpisode
        
        // Cuando estamos en modo serie completa, el rango siempre empieza desde S1E1
        _startRange.value = 1
        _selectedSeason.value = endSeason
        _endRange.value = endEpisode
    }

    fun setFromBeginning(value: Boolean) {
        _fromBeginning.value = value
        if (value) {
            // Si se activa "desde el principio", reseteamos el rango
            _startRange.value = 1
            _selectedSeason.value = 1
        }
    }

    fun loadMediaDetails(mediaId: String) {
        if (cachedMediaDetails != null && cachedParsedInfo != null) {
            _mediaState.value = MediaState.Success(
                media = cachedMediaDetails!!,
                parsedInfo = cachedParsedInfo
            )
            return
        }

        viewModelScope.launch {
            try {
                _mediaState.value = MediaState.Loading
                
                val mediaDetails = omdbService.getMediaDetails(imdbId = mediaId)
                cachedMediaDetails = mediaDetails

                val perplexityRequest = PerplexityRequest(
                    messages = listOf(
                        Message(
                            role = "system",
                            content = "You are a helpful assistant that provides information about movies and TV shows. Respond only with the requested JSON format, no additional text."
                        ),
                        Message(
                            role = "user",
                            content = when (mediaDetails.Type.lowercase()) {
                                "series" -> """
                                    For the TV show '${mediaDetails.Title}', tell me exactly how many episodes are in each season. 
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
                                    For the movie '${mediaDetails.Title}', tell me if it has any parts or sequels. 
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
                        ?.replace("```json", "")
                        ?.replace("```", "")
                        ?.trim()
                    
                    println("Perplexity response: $response") // Debug log
                    
                    if (response != null) {
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
                        MediaInfo.fromJson(it, mediaDetails.Type).also { info ->
                            println("Parsed info: $info") // Debug log
                        }
                    } catch (e: Exception) {
                        println("Error parsing JSON: ${e.message}") // Debug log
                        e.printStackTrace()
                        null
                    }
                }

                cachedParsedInfo = parsedInfo

                _mediaState.value = MediaState.Success(
                    media = mediaDetails,
                    parsedInfo = parsedInfo
                )
            } catch (e: Exception) {
                _mediaState.value = MediaState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    sealed class MediaState {
        object Loading : MediaState()
        data class Success(
            val media: DetailedMediaItem,
            val parsedInfo: MediaInfo?
        ) : MediaState()
        data class Error(val message: String) : MediaState()
    }
} 