package com.example.nospoilerapk.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nospoilerapk.data.network.PerplexityService
import com.example.nospoilerapk.data.network.Message
import com.example.nospoilerapk.data.network.PerplexityRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.JsonObject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val perplexityService: PerplexityService
) : ViewModel() {

    private val gson = Gson()

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val timelineState: StateFlow<TimelineState> = _timelineState

    fun getTimeline(mediaId: String, rangeStart: Int, rangeEnd: Int) {
        viewModelScope.launch {
            try {
                _timelineState.value = TimelineState.Loading
                
                val prompt = """
                    You are a JSON API that returns timelines of major events.
                    For the media with IMDB ID $mediaId, episodes $rangeStart to $rangeEnd,
                    return a JSON with this structure:
                    {
                        "events": [
                            "event 1 description",
                            "event 2 description",
                            "event 3 description"
                        ]
                    }
                    
                    Guidelines for events:
                    - List 5-7 major events in chronological order
                    - Each event should be a brief, clear description
                    - Focus on significant plot developments
                    - Keep descriptions general enough to avoid major spoilers
                    - Use present tense
                    
                    IMPORTANT: Return ONLY the JSON, no additional text or explanation.
                """.trimIndent()
                
                val response = perplexityService.getMediaInfo(
                    PerplexityRequest(
                        model = "llama-3.1-sonar-small-128k-online",
                        messages = listOf(Message("user", prompt))
                    )
                )

                val jsonResponse = response.choices.firstOrNull()?.message?.content
                    ?: throw Exception("No timeline generated")

                Log.d("TimelineViewModel", "API Response: $jsonResponse")

                val events = try {
                    val cleanJson = jsonResponse
                        .replace("```json", "")
                        .replace("```", "")
                        .replace("\n", "")
                        .trim()
                    
                    Log.d("TimelineViewModel", "Cleaned JSON: $cleanJson")
                    
                    if (!cleanJson.startsWith("{")) {
                        throw Exception("Invalid JSON format")
                    }
                    
                    val jsonObject = gson.fromJson(cleanJson, JsonObject::class.java)
                    if (!jsonObject.has("events")) {
                        throw Exception("JSON does not contain events array")
                    }
                    
                    jsonObject.getAsJsonArray("events").map { it.asString }
                } catch (e: Exception) {
                    Log.e("TimelineViewModel", "Parsing error", e)
                    throw Exception("Could not parse response: ${e.message}")
                }

                _timelineState.value = TimelineState.Success(events)
            } catch (e: Exception) {
                Log.e("TimelineViewModel", "Error getting timeline", e)
                _timelineState.value = TimelineState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    sealed class TimelineState {
        object Loading : TimelineState()
        data class Success(val events: List<String>) : TimelineState()
        data class Error(val message: String) : TimelineState()
    }
} 