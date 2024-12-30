package com.javhualde.nospoilerapk.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javhualde.nospoilerapk.data.network.PerplexityService
import com.javhualde.nospoilerapk.data.network.Message
import com.javhualde.nospoilerapk.data.network.PerplexityRequest
import com.javhualde.nospoilerapk.data.LanguageService
import com.javhualde.nospoilerapk.data.network.OmdbService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.javhualde.nospoilerapk.data.network.DetailedMediaItem

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val perplexityService: PerplexityService,
    private val omdbService: OmdbService,
    private val languageService: LanguageService
) : ViewModel() {

    private val gson = Gson()

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val timelineState: StateFlow<TimelineState> = _timelineState

    private val _mediaDetails = MutableStateFlow<DetailedMediaItem?>(null)
    val mediaDetails: StateFlow<DetailedMediaItem?> = _mediaDetails

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
                    Tu es une API JSON qui génère des chronologies détaillées d'événements importants.
                    Pour la série "$title", depuis l'épisode 1 de la saison 1 jusqu'à l'épisode $rangeEnd de la saison $season,
                    renvoie un JSON avec cette structure :
                    {
                        "events": [
                            "description d'un événement",
                            "description d'un autre événement",
                            "description d'un autre événement"
                        ]
                    }
                    
                    Directives pour les événements :
                    - Liste tous les événements significatifs par ordre chronologique
                    - Inclus les événements principaux et secondaires pertinents
                    - Chaque événement doit avoir une description claire et détaillée
                    - Concentre-toi sur les moments clés qui impactent l'intrigue
                    - Inclus les développements importants dans les relations entre personnages
                    - Écris en français
                    - Maintiens un ton neutre
                    - Évite les spoilers importants
                    - Utilise le présent
                    
                    IMPORTANT : Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            } else {
                """
                    Tu es une API JSON qui génère des chronologies détaillées d'événements importants.
                    Pour la série "$title", saison $season, épisodes $rangeStart à $rangeEnd,
                    renvoie un JSON avec cette structure :
                    {
                        "events": [
                            "description d'un événement",
                            "description d'un autre événement",
                            "description d'un autre événement"
                        ]
                    }
                    
                    Directives pour les événements :
                    - Liste tous les événements significatifs par ordre chronologique
                    - Inclus les événements principaux et secondaires pertinents
                    - Chaque événement doit avoir une description claire et détaillée
                    - Concentre-toi sur les moments clés qui impactent l'intrigue
                    - Inclus les développements importants dans les relations entre personnages
                    - Écris en français
                    - Maintiens un ton neutre
                    - Évite les spoilers importants
                    - Utilise le présent
                    
                    IMPORTANT : Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            }

            "de" -> if (isFromBeginning) {
                """
                    Du bist eine JSON-API, die detaillierte Zeitlinien wichtiger Ereignisse erstellt.
                    Für die Serie "$title", von Episode 1 der Staffel 1 bis Episode $rangeEnd der Staffel $season,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "events": [
                            "Beschreibung eines Ereignisses",
                            "Beschreibung eines weiteren Ereignisses",
                            "Beschreibung eines weiteren Ereignisses"
                        ]
                    }
                    
                    Richtlinien für die Ereignisse:
                    - Liste alle bedeutenden Ereignisse in chronologischer Reihenfolge auf
                    - Berücksichtige sowohl wichtige Haupt- als auch relevante Nebenereignisse
                    - Jedes Ereignis muss eine klare und detaillierte Beschreibung haben
                    - Konzentriere dich auf Schlüsselmomente, die die Handlung beeinflussen
                    - Berücksichtige wichtige Entwicklungen in den Beziehungen zwischen den Charakteren
                    - Schreibe auf Deutsch
                    - Behalte einen neutralen Ton bei
                    - Vermeide wichtige Spoiler
                    - Verwende die Gegenwartsform
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            } else {
                """
                    Du bist eine JSON-API, die detaillierte Zeitlinien wichtiger Ereignisse erstellt.
                    Für die Serie "$title", Staffel $season, Episoden $rangeStart bis $rangeEnd,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "events": [
                            "Beschreibung eines Ereignisses",
                            "Beschreibung eines weiteren Ereignisses",
                            "Beschreibung eines weiteren Ereignisses"
                        ]
                    }
                    
                    Richtlinien für die Ereignisse:
                    - Liste alle bedeutenden Ereignisse in chronologischer Reihenfolge auf
                    - Berücksichtige sowohl wichtige Haupt- als auch relevante Nebenereignisse
                    - Jedes Ereignis muss eine klare und detaillierte Beschreibung haben
                    - Konzentriere dich auf Schlüsselmomente, die die Handlung beeinflussen
                    - Berücksichtige wichtige Entwicklungen in den Beziehungen zwischen den Charakteren
                    - Schreibe auf Deutsch
                    - Behalte einen neutralen Ton bei
                    - Vermeide wichtige Spoiler
                    - Verwende die Gegenwartsform
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            }

            else -> if (isFromBeginning) {
                """
                    You are a JSON API that generates detailed timelines of important events.
                    For the TV show "$title", from episode 1 of season 1 to episode $rangeEnd of season $season,
                    return a JSON with this structure:
                    {
                        "events": [
                            "description of an event",
                            "description of another event",
                            "description of another event"
                        ]
                    }
                    
                    Guidelines for events:
                    - List all significant events in chronological order
                    - Include both major and relevant minor events
                    - Each event must have a clear and detailed description
                    - Focus on key moments that impact the plot
                    - Include important developments in character relationships
                    - Write in English
                    - Maintain a neutral tone
                    - Avoid major spoilers
                    - Use present tense
                    
                    IMPORTANT: Return ONLY the JSON, without additional text.
                """.trimIndent()
            } else {
                """
                    You are a JSON API that generates detailed timelines of important events.
                    For the TV show "$title", season $season, episodes $rangeStart to $rangeEnd,
                    return a JSON with this structure:
                    {
                        "events": [
                            "description of an event",
                            "description of another event",
                            "description of another event"
                        ]
                    }
                    
                    Guidelines for events:
                    - List all significant events in chronological order
                    - Include both major and relevant minor events
                    - Each event must have a clear and detailed description
                    - Focus on key moments that impact the plot
                    - Include important developments in character relationships
                    - Write in English
                    - Maintain a neutral tone
                    - Avoid major spoilers
                    - Use present tense
                    
                    IMPORTANT: Return ONLY the JSON, without additional text.
                """.trimIndent()
            }
        }
    }

    fun getTimeline(mediaId: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false) {
        viewModelScope.launch {
            try {
                _timelineState.value = TimelineState.Loading
                
                val details = omdbService.getMediaDetails(imdbId = mediaId)
                _mediaDetails.value = details
                
                val prompt = getPromptForLanguage(details.Title, rangeStart, rangeEnd, season, isFromBeginning)
                
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
                    
                    val jsonObject = gson.fromJson(cleanJson, JsonObject::class.java)
                    jsonObject.getAsJsonArray("events")
                        .map { it.asString.trim() }
                        .filter { it.isNotBlank() }
                        .map { it.trim('"') }
                        .map { it.replace(Regex("^\\d+\\.?\\s*"), "") }
                        .joinToString(" ")
                        .split(".")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { if (it.endsWith(".")) it else "$it." }

                } catch (e: Exception) {
                    Log.e("TimelineViewModel", "Error en el primer intento de parsing", e)
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