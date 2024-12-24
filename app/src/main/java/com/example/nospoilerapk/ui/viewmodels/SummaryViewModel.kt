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
import com.example.nospoilerapk.data.LanguageService
import com.example.nospoilerapk.data.network.OmdbService

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val perplexityService: PerplexityService,
    private val omdbService: OmdbService,
    private val languageService: LanguageService
) : ViewModel() {

    private val gson = Gson()

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Loading)
    val summaryState: StateFlow<SummaryState> = _summaryState

    private fun getPromptForLanguage(title: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false): String {
        val languageCode = languageService.getCurrentLanguageCode()
        return when (languageCode) {
            "es" -> if (isFromBeginning) {
                """
                    Eres una API JSON que genera resúmenes.
                    Para la serie llamada "$title", desde el episodio 1 de la temporada 1 hasta el episodio $rangeEnd de la temporada $season,
                    devuelve un JSON con esta estructura:
                    {
                        "summary": "el resumen aquí"
                    }
                    
                    Pautas para el resumen:
                    - Escribe en español
                    - Resume los eventos principales
                    - Mantén un tono neutral
                    - Evita spoilers importantes
                    - Usa tiempo presente
                    
                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            } else {
                """
                    Eres una API JSON que genera resúmenes.
                    Para la serie llamada "$title", temporada $season, episodios $rangeStart a $rangeEnd,
                    devuelve un JSON con esta estructura:
                    {
                        "summary": "el resumen aquí"
                    }
                    
                    Pautas para el resumen:
                    - Escribe en español
                    - Resume los eventos principales
                    - Mantén un tono neutral
                    - Evita spoilers importantes
                    - Usa tiempo presente
                    
                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            }
            
            "fr" -> if (isFromBeginning) {
                """
                    Tu es une API JSON qui génère des résumés.
                    Pour la série intitulée "$title", depuis l'épisode 1 de la saison 1 jusqu'à l'épisode $rangeEnd de la saison $season,
                    renvoie un JSON avec cette structure:
                    {
                        "summary": "le résumé ici"
                    }
                    
                    Directives pour le résumé:
                    - Écris en français
                    - Résume les événements principaux
                    - Maintiens un ton neutre
                    - Évite les spoilers importants
                    - Utilise le présent
                    
                    IMPORTANT: Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            } else {
                """
                    Tu es une API JSON qui génère des résumés.
                    Pour la série intitulée "$title", saison $season, épisodes $rangeStart à $rangeEnd,
                    renvoie un JSON avec cette structure:
                    {
                        "summary": "le résumé ici"
                    }
                    
                    Directives pour le résumé:
                    - Écris en français
                    - Résume les événements principaux
                    - Maintiens un ton neutre
                    - Évite les spoilers importants
                    - Utilise le présent
                    
                    IMPORTANT: Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            }
            
            "de" -> if (isFromBeginning) {
                """
                    Du bist eine JSON-API, die Zusammenfassungen generiert.
                    Für die Serie mit dem Titel "$title", von Episode 1 der Staffel 1 bis Episode $rangeEnd der Staffel $season,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "summary": "die Zusammenfassung hier"
                    }
                    
                    Richtlinien für die Zusammenfassung:
                    - Schreibe auf Deutsch
                    - Fasse die Hauptereignisse zusammen
                    - Behalte einen neutralen Ton
                    - Vermeide wichtige Spoiler
                    - Verwende Präsens
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            } else {
                """
                    Du bist eine JSON-API, die Zusammenfassungen generiert.
                    Für die Serie mit dem Titel "$title", Staffel $season, Episoden $rangeStart bis $rangeEnd,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "summary": "die Zusammenfassung hier"
                    }
                    
                    Richtlinien für die Zusammenfassung:
                    - Schreibe auf Deutsch
                    - Fasse die Hauptereignisse zusammen
                    - Behalte einen neutralen Ton
                    - Vermeide wichtige Spoiler
                    - Verwende Präsens
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            }
            
            else -> if (isFromBeginning) {
                """
                    You are a JSON API that generates summaries.
                    For the series titled "$title", from episode 1 of season 1 to episode $rangeEnd of season $season,
                    return a JSON with this structure:
                    {
                        "summary": "the summary here"
                    }
                    
                    Guidelines for the summary:
                    - Write in English
                    - Summarize main events
                    - Keep a neutral tone
                    - Avoid major spoilers
                    - Use present tense
                    
                    IMPORTANT: Return ONLY the JSON, no additional text.
                """.trimIndent()
            } else {
                """
                    You are a JSON API that generates summaries.
                    For the series titled "$title", season $season, episodes $rangeStart to $rangeEnd,
                    return a JSON with this structure:
                    {
                        "summary": "the summary here"
                    }
                    
                    Guidelines for the summary:
                    - Write in English
                    - Summarize main events
                    - Keep a neutral tone
                    - Avoid major spoilers
                    - Use present tense
                    
                    IMPORTANT: Return ONLY the JSON, no additional text.
                """.trimIndent()
            }
        }
    }

    fun getSummary(mediaId: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false) {
        viewModelScope.launch {
            try {
                _summaryState.value = SummaryState.Loading
                
                Log.d("SummaryViewModel", "==================================")
                Log.d("SummaryViewModel", "isFromBeginning: $isFromBeginning")
                Log.d("SummaryViewModel", "rangeStart: $rangeStart")
                Log.d("SummaryViewModel", "rangeEnd: $rangeEnd")
                Log.d("SummaryViewModel", "season: $season")
                Log.d("SummaryViewModel", "==================================")
                
                val mediaDetails = omdbService.getMediaDetails(imdbId = mediaId)
                val prompt = getPromptForLanguage(mediaDetails.Title, rangeStart, rangeEnd, season, isFromBeginning)
                
                Log.d("SummaryViewModel", "Generated prompt: $prompt")
                
                val response = perplexityService.getMediaInfo(
                    PerplexityRequest(
                        model = "llama-3.1-sonar-large-128k-online",
                        messages = listOf(Message("user", prompt))
                    )
                )

                val jsonResponse = response.choices.firstOrNull()?.message?.content
                    ?: throw Exception("No summary generated")

                Log.d("SummaryViewModel", "API Response: $jsonResponse")

                val summary = try {
                    val cleanJson = jsonResponse
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    
                    // Escapar caracteres problemáticos
                    val escapedJson = cleanJson
                        .replace("""\n""", " ")
                        .replace("\"", "'")
                        .replace("'", "\"") // Volver a poner comillas dobles para el JSON
                    
                    val jsonObject = gson.fromJson(escapedJson, JsonObject::class.java)
                    jsonObject.get("summary")?.asString ?: throw Exception("Summary field is null")
                } catch (e: Exception) {
                    Log.e("SummaryViewModel", "First parsing attempt failed", e)
                    
                    // Segundo intento: buscar el contenido entre comillas después de "summary":
                    try {
                        val pattern = "\"summary\"\\s*:\\s*\"(.*?)\"".toRegex(RegexOption.DOT_MATCHES_ALL)
                        val matchResult = pattern.find(jsonResponse)
                        matchResult?.groupValues?.get(1) ?: throw Exception("No summary found in response")
                    } catch (e: Exception) {
                        Log.e("SummaryViewModel", "Second parsing attempt failed", e)
                        throw Exception("Could not extract summary from response")
                    }
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