package com.example.nospoilerapk.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nospoilerapk.data.network.PerplexityService
import com.example.nospoilerapk.data.network.Message
import com.example.nospoilerapk.data.network.PerplexityRequest
import com.example.nospoilerapk.data.LanguageService
import com.example.nospoilerapk.data.network.OmdbService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.JsonObject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val perplexityService: PerplexityService,
    private val omdbService: OmdbService,
    private val languageService: LanguageService
) : ViewModel() {

    private val gson = Gson()

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val timelineState: StateFlow<TimelineState> = _timelineState

    private fun getPromptForLanguage(title: String, rangeStart: Int, rangeEnd: Int): String {
        val languageCode = languageService.getCurrentLanguageCode()
        return when (languageCode) {
            "es" -> """
                Eres una API JSON que devuelve líneas de tiempo de eventos importantes.
                Para la serie llamada "$title", episodios $rangeStart a $rangeEnd,
                devuelve un JSON con esta estructura:
                {
                    "events": [
                        "descripción de un evento",
                        "descripción de otro evento",
                        "descripción de otro evento más"
                    ]
                }
                
                Pautas para los eventos:
                - Lista 5-7 eventos principales en orden cronológico
                - Cada evento debe ser una descripción breve y clara
                - Enfócate en desarrollos importantes de la trama
                - Mantén las descripciones generales para evitar spoilers
                - Usa tiempo presente
                - No incluyas números o viñetas en las descripciones
                
                IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
            """.trimIndent()

            "fr" -> """
                Tu es une API JSON qui renvoie des chronologies d'événements majeurs.
                Pour la série intitulée "$title", épisodes $rangeStart à $rangeEnd,
                renvoie un JSON avec cette structure:
                {
                    "events": [
                        "description d'un événement",
                        "description d'un autre événement",
                        "description d'un autre événement"
                    ]
                }
                
                Directives pour les événements:
                - Liste 5-7 événements majeurs par ordre chronologique
                - Chaque événement doit être une description brève et claire
                - Concentre-toi sur les développements importants de l'intrigue
                - Garde les descriptions générales pour éviter les spoilers
                - Utilise le présent
                - N'inclus pas de numéros ou de puces dans les descriptions
                
                IMPORTANT: Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
            """.trimIndent()

            "de" -> """
                Du bist eine JSON-API, die Zeitlinien wichtiger Ereignisse zurückgibt.
                Für die Serie mit dem Titel "$title", Episoden $rangeStart bis $rangeEnd,
                gib ein JSON mit dieser Struktur zurück:
                {
                    "events": [
                        "Beschreibung eines Ereignisses",
                        "Beschreibung eines weiteren Ereignisses",
                        "Beschreibung eines weiteren Ereignisses"
                    ]
                }
                
                Richtlinien für die Ereignisse:
                - Liste 5-7 Hauptereignisse in chronologischer Reihenfolge auf
                - Jedes Ereignis sollte eine kurze, klare Beschreibung sein
                - Konzentriere dich auf wichtige Handlungsentwicklungen
                - Halte die Beschreibungen allgemein, um Spoiler zu vermeiden
                - Verwende Präsens
                - Füge keine Nummern oder Aufzählungszeichen in die Beschreibungen ein
                
                WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
            """.trimIndent()

            else -> """
                You are a JSON API that returns timelines of major events.
                For the series titled "$title", episodes $rangeStart to $rangeEnd,
                return a JSON with this structure:
                {
                    "events": [
                        "description of an event",
                        "description of another event",
                        "description of another event"
                    ]
                }
                
                Guidelines for events:
                - List 5-7 major events in chronological order
                - Each event should be a brief, clear description
                - Focus on significant plot developments
                - Keep descriptions general enough to avoid major spoilers
                - Use present tense
                - Do not include numbers or bullets in the descriptions
                
                IMPORTANT: Return ONLY the JSON, no additional text.
            """.trimIndent()
        }
    }

    fun getTimeline(mediaId: String, rangeStart: Int, rangeEnd: Int) {
        viewModelScope.launch {
            try {
                _timelineState.value = TimelineState.Loading
                
                val mediaDetails = omdbService.getMediaDetails(imdbId = mediaId)
                val prompt = getPromptForLanguage(mediaDetails.Title, rangeStart, rangeEnd)
                
                val response = perplexityService.getMediaInfo(
                    PerplexityRequest(
                        model = "llama-3.1-sonar-large-128k-online",
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
                        .trim()
                    
                    // Escapar caracteres problemáticos
                    val escapedJson = cleanJson
                        .replace("""\n""", " ")
                        .replace("\"", "'")
                    
                    val jsonObject = gson.fromJson(escapedJson, JsonObject::class.java)
                    jsonObject.getAsJsonArray("events").map { it.asString }
                } catch (e: Exception) {
                    Log.e("TimelineViewModel", "Parsing error", e)
                    // Intento alternativo usando regex
                    val eventsMatch = Regex(""""events":\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
                        .find(jsonResponse)?.groupValues?.get(1)
                    
                    eventsMatch?.split(",")
                        ?.map { it.trim().removeSurrounding("\"") }
                        ?: throw Exception("Could not parse response: ${e.message}")
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