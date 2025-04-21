package com.javhualde.nospoilerapk.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javhualde.nospoilerapk.data.network.XAIService
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
    private val xaiService: XAIService,
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
                    Eres una API JSON que devuelve líneas de tiempo de eventos importantes organizados en secciones.
                    Para la serie "$title", desde el episodio 1 de la temporada 1 hasta el episodio $rangeEnd de la temporada $season,
                    devuelve un JSON con esta estructura:
                    {
                        "sections": [
                            {
                                "title": "Título de la sección 1",
                                "events": [
                                    "descripción del evento 1",
                                    "descripción del evento 2"
                                ]
                            },
                            {
                                "title": "Título de la sección 2",
                                "events": [
                                    "descripción del evento 3",
                                    "descripción del evento 4"
                                ]
                            }
                        ]
                    }
                    
                    Pautas para las secciones y eventos:
                    - Organiza los eventos en 3-5 secciones lógicas basadas en arcos argumentales o temas principales
                    - Cada sección debe tener un título descriptivo que refleje su tema o período
                    - Incluye 2-4 eventos relevantes por sección
                    - Los eventos deben estar en orden cronológico dentro de cada sección
                    - Enfócate en desarrollos importantes de la trama y relaciones entre personajes
                    - Mantén las descripciones breves pero informativas
                    - Usa tiempo presente
                    - Evita spoilers importantes
                    - No incluyas números o viñetas en las descripciones
                    
                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            } else {
                """
                    Eres una API JSON que devuelve líneas de tiempo de eventos importantes organizados en secciones.
                    Para la serie "$title", temporada $season, episodios $rangeStart a $rangeEnd,
                    devuelve un JSON con esta estructura:
                    {
                        "sections": [
                            {
                                "title": "Título de la sección 1",
                                "events": [
                                    "descripción del evento 1",
                                    "descripción del evento 2"
                                ]
                            },
                            {
                                "title": "Título de la sección 2",
                                "events": [
                                    "descripción del evento 3",
                                    "descripción del evento 4"
                                ]
                            }
                        ]
                    }
                    
                    Pautas para las secciones y eventos:
                    - Organiza los eventos en 3-5 secciones lógicas basadas en arcos argumentales o temas principales
                    - Cada sección debe tener un título descriptivo que refleje su tema o período
                    - Incluye 2-4 eventos relevantes por sección
                    - Los eventos deben estar en orden cronológico dentro de cada sección
                    - Enfócate en desarrollos importantes de la trama y relaciones entre personajes
                    - Mantén las descripciones breves pero informativas
                    - Usa tiempo presente
                    - Evita spoilers importantes
                    - No incluyas números o viñetas en las descripciones
                    
                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            }
            "fr" -> if (isFromBeginning) {
                """
                    Tu es une API JSON qui génère des chronologies d'événements importants organisées en sections.
                    Pour la série "$title", depuis l'épisode 1 de la saison 1 jusqu'à l'épisode $rangeEnd de la saison $season,
                    renvoie un JSON avec cette structure :
                    {
                        "sections": [
                            {
                                "title": "Titre de la section 1",
                                "events": [
                                    "description de l'événement 1",
                                    "description de l'événement 2"
                                ]
                            },
                            {
                                "title": "Titre de la section 2",
                                "events": [
                                    "description de l'événement 3",
                                    "description de l'événement 4"
                                ]
                            }
                        ]
                    }
                    
                    Directives pour les sections et événements :
                    - Organise les événements en 3-5 sections logiques basées sur des arcs narratifs ou des thèmes principaux
                    - Chaque section doit avoir un titre descriptif qui reflète son thème ou sa période
                    - Inclus 2-4 événements pertinents par section
                    - Les événements doivent être dans l'ordre chronologique au sein de chaque section
                    - Concentre-toi sur les développements importants de l'intrigue et les relations entre personnages
                    - Garde les descriptions concises mais informatives
                    - Utilise le présent
                    - Évite les spoilers importants
                    - N'inclus pas de numéros ou de puces dans les descriptions
                    
                    IMPORTANT : Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            } else {
                """
                    Tu es une API JSON qui génère des chronologies d'événements importants organisées en sections.
                    Pour la série "$title", saison $season, épisodes $rangeStart à $rangeEnd,
                    renvoie un JSON avec cette structure :
                    {
                        "sections": [
                            {
                                "title": "Titre de la section 1",
                                "events": [
                                    "description de l'événement 1",
                                    "description de l'événement 2"
                                ]
                            },
                            {
                                "title": "Titre de la section 2",
                                "events": [
                                    "description de l'événement 3",
                                    "description de l'événement 4"
                                ]
                            }
                        ]
                    }
                    
                    Directives pour les sections et événements :
                    - Organise les événements en 3-5 sections logiques basées sur des arcs narratifs ou des thèmes principaux
                    - Chaque section doit avoir un titre descriptif qui reflète son thème ou sa période
                    - Inclus 2-4 événements pertinents par section
                    - Les événements doivent être dans l'ordre chronologique au sein de chaque section
                    - Concentre-toi sur les développements importants de l'intrigue et les relations entre personnages
                    - Garde les descriptions concises mais informatives
                    - Utilise le présent
                    - Évite les spoilers importants
                    - N'inclus pas de numéros ou de puces dans les descriptions
                    
                    IMPORTANT : Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            }
            "de" -> if (isFromBeginning) {
                """
                    Du bist eine JSON-API, die Zeitlinien wichtiger Ereignisse in Abschnitten organisiert.
                    Für die Serie "$title", von Episode 1 der Staffel 1 bis Episode $rangeEnd der Staffel $season,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "sections": [
                            {
                                "title": "Titel des Abschnitts 1",
                                "events": [
                                    "Beschreibung des Ereignisses 1",
                                    "Beschreibung des Ereignisses 2"
                                ]
                            },
                            {
                                "title": "Titel des Abschnitts 2",
                                "events": [
                                    "Beschreibung des Ereignisses 3",
                                    "Beschreibung des Ereignisses 4"
                                ]
                            }
                        ]
                    }
                    
                    Richtlinien für Abschnitte und Ereignisse:
                    - Organisiere die Ereignisse in 3-5 logische Abschnitte basierend auf Handlungsbögen oder Hauptthemen
                    - Jeder Abschnitt muss einen beschreibenden Titel haben, der sein Thema oder seine Periode widerspiegelt
                    - Füge 2-4 relevante Ereignisse pro Abschnitt ein
                    - Die Ereignisse müssen innerhalb jedes Abschnitts chronologisch geordnet sein
                    - Konzentriere dich auf wichtige Handlungsentwicklungen und Beziehungen zwischen Charakteren
                    - Halte die Beschreibungen prägnant aber informativ
                    - Verwende die Gegenwartsform
                    - Vermeide wichtige Spoiler
                    - Füge keine Nummern oder Aufzählungszeichen in die Beschreibungen ein
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            } else {
                """
                    Du bist eine JSON-API, die Zeitlinien wichtiger Ereignisse in Abschnitten organisiert.
                    Für die Serie "$title", Staffel $season, Episoden $rangeStart bis $rangeEnd,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "sections": [
                            {
                                "title": "Titel des Abschnitts 1",
                                "events": [
                                    "Beschreibung des Ereignisses 1",
                                    "Beschreibung des Ereignisses 2"
                                ]
                            },
                            {
                                "title": "Titel des Abschnitts 2",
                                "events": [
                                    "Beschreibung des Ereignisses 3",
                                    "Beschreibung des Ereignisses 4"
                                ]
                            }
                        ]
                    }
                    
                    Richtlinien für Abschnitte und Ereignisse:
                    - Organisiere die Ereignisse in 3-5 logische Abschnitte basierend auf Handlungsbögen oder Hauptthemen
                    - Jeder Abschnitt muss einen beschreibenden Titel haben, der sein Thema oder seine Periode widerspiegelt
                    - Füge 2-4 relevante Ereignisse pro Abschnitt ein
                    - Die Ereignisse müssen innerhalb jedes Abschnitts chronologisch geordnet sein
                    - Konzentriere dich auf wichtige Handlungsentwicklungen und Beziehungen zwischen Charakteren
                    - Halte die Beschreibungen prägnant aber informativ
                    - Verwende die Gegenwartsform
                    - Vermeide wichtige Spoiler
                    - Füge keine Nummern oder Aufzählungszeichen in die Beschreibungen ein
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            }
            else -> if (isFromBeginning) {
                """
                    You are a JSON API that generates timelines of important events organized in sections.
                    For the TV show "$title", from episode 1 of season 1 to episode $rangeEnd of season $season,
                    return a JSON with this structure:
                    {
                        "sections": [
                            {
                                "title": "Section 1 Title",
                                "events": [
                                    "description of event 1",
                                    "description of event 2"
                                ]
                            },
                            {
                                "title": "Section 2 Title",
                                "events": [
                                    "description of event 3",
                                    "description of event 4"
                                ]
                            }
                        ]
                    }
                    
                    Guidelines for sections and events:
                    - Organize events into 3-5 logical sections based on story arcs or main themes
                    - Each section must have a descriptive title that reflects its theme or period
                    - Include 2-4 relevant events per section
                    - Events must be in chronological order within each section
                    - Focus on important plot developments and character relationships
                    - Keep descriptions concise but informative
                    - Use present tense
                    - Avoid major spoilers
                    - Do not include numbers or bullet points in descriptions
                    
                    IMPORTANT: Return ONLY the JSON, without additional text.
                """.trimIndent()
            } else {
                """
                    You are a JSON API that generates timelines of important events organized in sections.
                    For the TV show "$title", season $season, episodes $rangeStart to $rangeEnd,
                    return a JSON with this structure:
                    {
                        "sections": [
                            {
                                "title": "Section 1 Title",
                                "events": [
                                    "description of event 1",
                                    "description of event 2"
                                ]
                            },
                            {
                                "title": "Section 2 Title",
                                "events": [
                                    "description of event 3",
                                    "description of event 4"
                                ]
                            }
                        ]
                    }
                    
                    Guidelines for sections and events:
                    - Organize events into 3-5 logical sections based on story arcs or main themes
                    - Each section must have a descriptive title that reflects its theme or period
                    - Include 2-4 relevant events per section
                    - Events must be in chronological order within each section
                    - Focus on important plot developments and character relationships
                    - Keep descriptions concise but informative
                    - Use present tense
                    - Avoid major spoilers
                    - Do not include numbers or bullet points in descriptions
                    
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
                
                val response = xaiService.getMediaInfo(
                    PerplexityRequest(
                        model = "grok-3-mini-beta",
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
                    jsonObject.getAsJsonArray("sections")
                        .map { it.asJsonObject }
                        .map {
                            val title = it.get("title").asString.trim()
                            val events = it.getAsJsonArray("events")
                                .map { it.asString.trim() }
                                .filter { it.isNotBlank() }
                                .map { it.trim('"') }
                                .map { it.replace(Regex("^\\d+\\.?\\s*"), "") }
                                .joinToString(" ")
                                .split(".")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .map { if (it.endsWith(".")) it else "$it." }
                            Pair(title, events)
                        }
                        .toMap()

                } catch (e: Exception) {
                    Log.e("TimelineViewModel", "Error en el primer intento de parsing", e)
                    jsonResponse
                        .replace(Regex("[\"\\[\\]{}]"), "")
                        .split("sections:")
                        .last()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { it.split(":") }
                        .map { (title, events) ->
                            val title = title.trim()
                            val eventList = events.trim().split(",").map { it.trim() }
                            Pair(title, eventList)
                        }
                        .toMap()
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
        data class Success(val events: Map<String, List<String>>) : TimelineState()
        data class Error(val message: String) : TimelineState()
    }
} 