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

    private fun getPromptForLanguage(title: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false): String {
        val languageCode = languageService.getCurrentLanguageCode()
        return when (languageCode) {
            "es" -> if (isFromBeginning) {
                """
                    Eres una API JSON que devuelve líneas de tiempo de eventos importantes.
                    Para la serie llamada "$title", desde el episodio 1 de la temporada 1 hasta el episodio $rangeEnd de la temporada $season,
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
            } else {
                """
                    Eres una API JSON que devuelve líneas de tiempo de eventos importantes.
                    Para la serie llamada "$title", temporada $season, episodios $rangeStart a $rangeEnd,
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
            }

            "fr" -> if (isFromBeginning) {
                """
                    Tu es une API JSON qui renvoie des chronologies d'événements majeurs.
                    Pour la série intitulée "$title", depuis l'épisode 1 de la saison 1 jusqu'à l'épisode $rangeEnd de la saison $season,
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
            } else {
                """
                    Tu es une API JSON qui renvoie des chronologies d'événements majeurs.
                    Pour la série intitulée "$title", saison $season, épisodes $rangeStart à $rangeEnd,
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
            }

            "de" -> if (isFromBeginning) {
                """
                    Du bist eine JSON-API, die Zeitlinien wichtiger Ereignisse zurückgibt.
                    Für die Serie mit dem Titel "$title", von Episode 1 der Staffel 1 bis Episode $rangeEnd der Staffel $season,
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
            } else {
                """
                    Du bist eine JSON-API, die Zeitlinien wichtiger Ereignisse zurückgibt.
                    Für die Serie mit dem Titel "$title", Staffel $season, Episoden $rangeStart bis $rangeEnd,
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
            }

            else -> if (isFromBeginning) {
                """
                    You are a JSON API that returns timelines of major events.
                    For the series titled "$title", from episode 1 of season 1 to episode $rangeEnd of season $season,
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
            } else {
                """
                    You are a JSON API that returns timelines of major events.
                    For the series titled "$title", season $season, episodes $rangeStart to $rangeEnd,
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
    }

    fun getTimeline(mediaId: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false) {
        viewModelScope.launch {
            try {
                _timelineState.value = TimelineState.Loading
                
                val mediaDetails = omdbService.getMediaDetails(imdbId = mediaId)
                val prompt = getPromptForLanguage(mediaDetails.Title, rangeStart, rangeEnd, season, isFromBeginning)
                
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
                    
                    // Simplificar el proceso de limpieza
                    val jsonObject = gson.fromJson(cleanJson, JsonObject::class.java)
                    jsonObject.getAsJsonArray("events")
                        .map { it.asString.trim() }
                        // Filtrar eventos vacíos o que solo contengan espacios
                        .filter { it.isNotBlank() }
                        // Eliminar comillas extras si las hay
                        .map { it.trim('"') }
                        // Eliminar cualquier numeración al inicio si existe
                        .map { it.replace(Regex("^\\d+\\.?\\s*"), "") }
                        // Unir fragmentos que puedan haber sido separados incorrectamente
                        .joinToString(" ")
                        // Volver a dividir por puntos finales para tener eventos separados
                        .split(".")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        // Asegurarse de que cada evento termine con punto
                        .map { if (it.endsWith(".")) it else "$it." }

                } catch (e: Exception) {
                    Log.e("TimelineViewModel", "Error en el primer intento de parsing", e)
                    // Intento alternativo más simple si el primer método falla
                    jsonResponse
                        .replace(Regex("[\"\\[\\]{}]"), "")
                        .split("events:")
                        .last()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { if (it.endsWith(".")) it else "$it." }
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