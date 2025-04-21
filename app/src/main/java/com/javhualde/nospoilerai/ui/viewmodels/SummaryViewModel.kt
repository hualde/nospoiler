package com.javhualde.nospoilerapk.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javhualde.nospoilerapk.data.network.XAIService
import com.javhualde.nospoilerapk.data.network.Message
import com.javhualde.nospoilerapk.data.network.PerplexityRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.javhualde.nospoilerapk.data.LanguageService
import com.javhualde.nospoilerapk.data.network.OmdbService
import kotlinx.coroutines.async
import com.javhualde.nospoilerapk.data.network.DetailedMediaItem
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val xaiService: XAIService,
    private val omdbService: OmdbService,
    private val languageService: LanguageService,
    private val httpClient: OkHttpClient
) : ViewModel() {

    private val gson = Gson()

    private val _state = MutableStateFlow(SummaryScreenState())
    val state = _state.asStateFlow()

    // Añadir un Job para manejar las corrutinas
    private var currentJob: Job? = null

    private fun getPromptForLanguage(title: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false): String {
        val languageCode = languageService.getCurrentLanguageCode()
        return when (languageCode) {
            "es" -> if (isFromBeginning) {
                """
                    Eres una API JSON que genera resúmenes detallados y extensos.
                    Para la serie llamada "$title", desde el episodio 1 de la temporada 1 hasta el episodio $rangeEnd de la temporada $season,
                    devuelve un JSON con esta estructura:
                    {
                        "summary": "el resumen aquí"
                    }
                    
                    Pautas para el resumen:
                    - Escribe en español
                    - Proporciona un resumen extenso y detallado
                    - Incluye desarrollo de personajes y arcos argumentales
                    - Describe las subtramas importantes
                    - Menciona el contexto y las motivaciones de los personajes
                    - Explica las relaciones entre personajes
                    - Mantén un tono neutral
                    - Evita spoilers importantes sobre muertes o giros dramáticos
                    - Usa tiempo presente
                    - Mínimo 500 palabras
                    
                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            } else {
                """
                    Eres una API JSON que genera resúmenes detallados y extensos.
                    Para la serie llamada "$title", temporada $season, episodios $rangeStart a $rangeEnd,
                    devuelve un JSON con esta estructura:
                    {
                        "summary": "el resumen aquí"
                    }
                    
                    Pautas para el resumen:
                    - Escribe en español
                    - Proporciona un resumen extenso y detallado
                    - Incluye desarrollo de personajes y arcos argumentales
                    - Describe las subtramas importantes
                    - Menciona el contexto y las motivaciones de los personajes
                    - Explica las relaciones entre personajes
                    - Mantén un tono neutral
                    - Evita spoilers importantes sobre muertes o giros dramáticos
                    - Usa tiempo presente
                    - Mínimo 500 palabras
                    
                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            }
            
            "fr" -> if (isFromBeginning) {
                """
                    Tu es une API JSON qui génère des résumés détaillés et approfondis.
                    Pour la série intitulée "$title", depuis l'épisode 1 de la saison 1 jusqu'à l'épisode $rangeEnd de la saison $season,
                    renvoie un JSON avec cette structure:
                    {
                        "summary": "le résumé ici"
                    }
                    
                    Directives pour le résumé:
                    - Écris en français
                    - Fournis un résumé détaillé et approfondi
                    - Inclus le développement des personnages et les arcs narratifs
                    - Décris les intrigues secondaires importantes
                    - Mentionne le contexte et les motivations des personnages
                    - Explique les relations entre les personnages
                    - Maintiens un ton neutre
                    - Évite les spoilers importants concernant les morts ou les rebondissements majeurs
                    - Utilise le présent
                    - Minimum 500 mots
                    
                    IMPORTANT: Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            } else {
                """
                    Tu es une API JSON qui génère des résumés détaillés et approfondis.
                    Pour la série intitulée "$title", saison $season, épisodes $rangeStart à $rangeEnd,
                    renvoie un JSON avec cette structure:
                    {
                        "summary": "le résumé ici"
                    }
                    
                    Directives pour le résumé:
                    - Écris en français
                    - Fournis un résumé détaillé et approfondi
                    - Inclus le développement des personnages et les arcs narratifs
                    - Décris les intrigues secondaires importantes
                    - Mentionne le contexte et les motivations des personnages
                    - Explique les relations entre les personnages
                    - Maintiens un ton neutre
                    - Évite les spoilers importants concernant les morts ou les rebondissements majeurs
                    - Utilise le présent
                    - Minimum 500 mots
                    
                    IMPORTANT: Renvoie UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            }
            
            "de" -> if (isFromBeginning) {
                """
                    Du bist eine JSON-API, die detaillierte und umfassende Zusammenfassungen generiert.
                    Für die Serie mit dem Titel "$title", von Episode 1 der Staffel 1 bis Episode $rangeEnd der Staffel $season,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "summary": "die Zusammenfassung hier"
                    }
                    
                    Richtlinien für die Zusammenfassung:
                    - Schreibe auf Deutsch
                    - Liefere eine detaillierte und umfassende Zusammenfassung
                    - Berücksichtige Charakterentwicklung und Handlungsbögen
                    - Beschreibe wichtige Nebenhandlungen
                    - Erwähne Kontext und Motivationen der Charaktere
                    - Erkläre die Beziehungen zwischen den Charakteren
                    - Behalte einen neutralen Ton
                    - Vermeide wichtige Spoiler zu Todesfällen oder dramatischen Wendungen
                    - Verwende Präsens
                    - Mindestens 500 Wörter
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            } else {
                """
                    Du bist eine JSON-API, die detaillierte und umfassende Zusammenfassungen generiert.
                    Für die Serie mit dem Titel "$title", Staffel $season, Episoden $rangeStart bis $rangeEnd,
                    gib ein JSON mit dieser Struktur zurück:
                    {
                        "summary": "die Zusammenfassung hier"
                    }
                    
                    Richtlinien für die Zusammenfassung:
                    - Schreibe auf Deutsch
                    - Liefere eine detaillierte und umfassende Zusammenfassung
                    - Berücksichtige Charakterentwicklung und Handlungsbögen
                    - Beschreibe wichtige Nebenhandlungen
                    - Erwähne Kontext und Motivationen der Charaktere
                    - Erkläre die Beziehungen zwischen den Charakteren
                    - Behalte einen neutralen Ton
                    - Vermeide wichtige Spoiler zu Todesfällen oder dramatischen Wendungen
                    - Verwende Präsens
                    - Mindestens 500 Wörter
                    
                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            }
            
            else -> if (isFromBeginning) {
                """
                    You are a JSON API that generates detailed and comprehensive summaries.
                    For the series titled "$title", from episode 1 of season 1 to episode $rangeEnd of season $season,
                    return a JSON with this structure:
                    {
                        "summary": "the summary here"
                    }
                    
                    Guidelines for the summary:
                    - Write in English
                    - Provide a detailed and comprehensive summary
                    - Include character development and story arcs
                    - Describe important subplots
                    - Mention context and character motivations
                    - Explain relationships between characters
                    - Keep a neutral tone
                    - Avoid major spoilers about deaths or dramatic twists
                    - Use present tense
                    - Minimum 500 words
                    
                    IMPORTANT: Return ONLY the JSON, no additional text.
                """.trimIndent()
            } else {
                """
                    You are a JSON API that generates detailed and comprehensive summaries.
                    For the series titled "$title", season $season, episodes $rangeStart to $rangeEnd,
                    return a JSON with this structure:
                    {
                        "summary": "the summary here"
                    }
                    
                    Guidelines for the summary:
                    - Write in English
                    - Provide a detailed and comprehensive summary
                    - Include character development and story arcs
                    - Describe important subplots
                    - Mention context and character motivations
                    - Explain relationships between characters
                    - Keep a neutral tone
                    - Avoid major spoilers about deaths or dramatic twists
                    - Use present tense
                    - Minimum 500 words
                    
                    IMPORTANT: Return ONLY the JSON, no additional text.
                """.trimIndent()
            }
        }
    }

    private suspend fun fetchSummaryFromApi(
        mediaId: String,
        rangeStart: Int,
        rangeEnd: Int,
        season: Int,
        isFromBeginning: Boolean
    ): String = withContext(Dispatchers.IO) {
        val mediaDetails = omdbService.getMediaDetails(imdbId = mediaId)
        val prompt = getPromptForLanguage(mediaDetails.Title, rangeStart, rangeEnd, season, isFromBeginning)
        
        val response = xaiService.getMediaInfo(
            PerplexityRequest(
                model = "grok-3-mini-beta",
                messages = listOf(
                    Message(
                        role = "system",
                        content = "You are a helpful assistant that provides information about movies and TV shows. Respond only with the requested JSON format, no additional text."
                    ),
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )
        )

        val jsonResponse = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("No summary generated")

        // Extraer directamente el contenido del resumen usando regex
        val summaryPattern = "\"summary\"\\s*:\\s*\"(.*?)(?:\"|$)".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matchResult = summaryPattern.find(jsonResponse)
        
        matchResult?.groupValues?.get(1)?.let { summary ->
            // Limpiar el resumen de caracteres de escape
            summary
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\t", "\t")
        } ?: throw Exception("Could not extract summary from response")
    }

    private suspend fun getActorImage(actorName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val encodedName = actorName
                    .replace(" ", "_")
                    .replace("'", "%27")
                    .replace("\"", "")
                
                val url = "https://commons.wikimedia.org/w/api.php?" +
                    "action=query&" +
                    "format=json&" +
                    "prop=imageinfo&" +
                    "iiprop=url&" +
                    "generator=search&" +
                    "gsrnamespace=6&" +
                    "gsrlimit=10&" +
                    "gsrsearch=filetype:bitmap|drawing|jpg|jpeg|png " +
                    "\"${encodedName}\" actor"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "NoSpoilerAI/1.0 (javhualde@gmail.com)")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    
                    val jsonResponse = response.body?.string() ?: return@withContext null
                    Log.d("SummaryViewModel", "Response for $actorName: $jsonResponse")
                    
                    val jsonObject = gson.fromJson(jsonResponse, JsonObject::class.java)
                    
                    val query = jsonObject.getAsJsonObject("query") ?: return@withContext null
                    val pages = query.getAsJsonObject("pages") ?: return@withContext null
                    
                    pages.entrySet().asSequence()
                        .mapNotNull { entry ->
                            val page = entry.value.asJsonObject
                            val title = page.get("title")?.asString ?: return@mapNotNull null
                            val imageInfo = page.getAsJsonArray("imageinfo")
                                ?.get(0)?.asJsonObject
                                ?.get("url")?.asString

                            if (title.contains("svg", ignoreCase = true) ||
                                title.contains("logo", ignoreCase = true) ||
                                title.contains("icon", ignoreCase = true) ||
                                title.contains("symbol", ignoreCase = true)) {
                                null
                            } else {
                                imageInfo
                            }
                        }
                        .firstOrNull()
                }
            } catch (e: Exception) {
                Log.e("SummaryViewModel", "Error getting actor image for $actorName", e)
                null
            }
        }
    }

    fun loadSummary(mediaId: String, rangeStart: Int, rangeEnd: Int, season: Int, isFromBeginning: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                Log.d("SummaryViewModel", "==================================")
                Log.d("SummaryViewModel", "isFromBeginning: $isFromBeginning")
                Log.d("SummaryViewModel", "rangeStart: $rangeStart")
                Log.d("SummaryViewModel", "rangeEnd: $rangeEnd")
                Log.d("SummaryViewModel", "season: $season")
                Log.d("SummaryViewModel", "==================================")
                
                val summary = fetchSummaryFromApi(mediaId, rangeStart, rangeEnd, season, isFromBeginning)

                _state.value = _state.value.copy(
                    mediaDetails = omdbService.getMediaDetails(imdbId = mediaId),
                    summary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("SummaryViewModel", "Error getting summary", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadContent(
        mediaId: String,
        season: Int,
        rangeStart: Int,
        rangeEnd: Int,
        isFromBeginning: Boolean
    ) {
        // Cancelar el job anterior si existe
        currentJob?.cancel()
        
        // Crear un nuevo job
        currentJob = viewModelScope.launch {
            try {
                // Primero cargamos los detalles del media
                val mediaDetails = withContext(Dispatchers.IO) {
                    omdbService.getMediaDetails(imdbId = mediaId)
                }
                
                // Actualizamos el estado con los detalles básicos
                _state.value = _state.value.copy(
                    mediaDetails = mediaDetails,
                    isLoading = true,
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                    season = season
                )

                // Luego cargamos el resumen y las imágenes de actores
                val summary = withContext(Dispatchers.IO) {
                    fetchSummaryFromApi(mediaId, rangeStart, rangeEnd, season, isFromBeginning)
                }
                
                val actorImages = withContext(Dispatchers.IO) {
                    coroutineScope {
                        mediaDetails.Actors.split(",")
                            .map { it.trim() }
                            .map { actorName ->
                                async {
                                    val imageUrl = getActorImage(actorName)
                                    if (imageUrl != null) {
                                        Pair(actorName, imageUrl)
                                    } else null
                                }
                            }
                            .awaitAll()
                            .filterNotNull()
                            .toMap()
                    }
                }

                // Actualizamos el estado con toda la información
                _state.value = _state.value.copy(
                    summary = summary,
                    actorImages = actorImages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    // Limpiar recursos cuando se destruye el ViewModel
    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    data class SummaryScreenState(
        val mediaDetails: DetailedMediaItem? = null,
        val summary: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val rangeStart: Int = 0,
        val rangeEnd: Int = 0,
        val season: Int = 1,
        val actorImages: Map<String, String> = emptyMap() // Nombre del actor -> URL de la imagen
    )
} 