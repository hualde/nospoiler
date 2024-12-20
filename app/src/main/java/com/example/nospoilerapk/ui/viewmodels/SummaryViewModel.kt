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
class SummaryViewModel @Inject constructor(
    private val perplexityService: PerplexityService
) : ViewModel() {

    private val gson = Gson()

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Loading)
    val summaryState: StateFlow<SummaryState> = _summaryState

    fun getSummary(mediaId: String, rangeStart: Int, rangeEnd: Int) {
        viewModelScope.launch {
            try {
                _summaryState.value = SummaryState.Loading
                
                val prompt = """
                    You are a JSON API that returns comprehensive spoiler-free summaries. 
                    For the media with IMDB ID $mediaId, episodes $rangeStart to $rangeEnd, 
                    provide a detailed analysis in JSON format.
                    Return a JSON with this structure:
                    {"summary": "your detailed summary here"}
                    
                    Guidelines for the summary:
                    - Provide a comprehensive overview (300-400 words) that helps viewers catch up with the story
                    - Explain the main events and developments in a general way
                    - Focus on what viewers need to know to continue watching
                    - Include important character introductions and group dynamics
                    - Mention key locations and important elements introduced
                    - Keep major twists and revelations vague but acknowledged
                    - Balance between being informative and avoiding specific spoilers
                    
                    IMPORTANT: Return ONLY the JSON, no additional text or explanation.
                """.trimIndent()
                
                val response = perplexityService.getMediaInfo(
                    PerplexityRequest(
                        model = "llama-3.1-sonar-large-128k-online",
                        messages = listOf(Message("user", prompt))
                    )
                )

                val jsonResponse = response.choices.firstOrNull()?.message?.content
                    ?: throw Exception("No summary generated")

                Log.d("SummaryViewModel", "API Response: $jsonResponse")

                // Limpiar y extraer solo el contenido del summary
                val summary = try {
                    val cleanJson = jsonResponse
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    
                    val jsonObject = gson.fromJson(cleanJson, JsonObject::class.java)
                    jsonObject.get("summary").asString
                } catch (e: Exception) {
                    Log.e("SummaryViewModel", "Parsing error", e)
                    throw Exception("Could not parse response: ${e.message}")
                }

                _summaryState.value = SummaryState.Success(summary)
            } catch (e: Exception) {
                Log.e("SummaryViewModel", "Error getting summary", e)
                _summaryState.value = SummaryState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    sealed class SummaryState {
        object Loading : SummaryState()
        data class Success(val summary: String) : SummaryState()
        data class Error(val message: String) : SummaryState()
    }
} 