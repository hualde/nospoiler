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
        val episodeCount = if (isFromBeginning) rangeEnd else rangeEnd - rangeStart + 1
        val minWords = when {
            episodeCount > 5 -> 1000
            episodeCount > 1 -> 500
            else -> 300
        }
        
        return when (languageCode) {
            "es" -> if (isFromBeginning) {
                """
                    Eres una API JSON que genera resúmenes detallados y extensos de series de televisión.
                    Para la serie "$title", proporciona un resumen que cubra desde el episodio 1 de la temporada 1 hasta el episodio $rangeEnd de la temporada $season.
                    Devuelve un JSON con esta estructura:
                    {
                        "summary": "el resumen aquí"
                    }

                    Pautas para el resumen:
                    - Escribe en español, adaptándote al estilo narrativo natural del idioma.
                    - Proporciona un resumen extenso, detallado y cohesionado, integrando los eventos en una narrativa fluida en lugar de listar episodios.
                    - Destaca los momentos clave de la trama, las interacciones significativas entre personajes y los desarrollos importantes de la historia.
                    - Describe las transformaciones o evoluciones de los personajes principales, con ejemplos concretos de sus decisiones o acciones.
                    - Menciona las subtramas relevantes y explica cómo se conectan con la trama principal o los personajes clave.
                    - Incluye el contexto y las motivaciones de los personajes, ilustrándolas con ejemplos específicos.
                    - Explica las relaciones entre personajes, detallando dinámicas de poder, alianzas, conflictos o vínculos románticos, según corresponda.
                    - Si es la primera mención de un personaje principal, incluye una breve descripción (ejemplo: "Ana, una detective astuta y reservada").
                    - Mantén un tono neutral, evitando lenguaje sensacionalista o emotivo.
                    - Evita spoilers importantes, como muertes de personajes principales o giros que alteren fundamentalmente la trama.
                    - Usa el tiempo presente.
                    - El resumen debe tener al menos $minWords palabras, ajustándose a la cantidad de episodios cubiertos.
                    - Concluye con una frase o pregunta intrigante que invite a ver los siguientes episodios, sin revelar spoilers.

                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            } else {
                """
                    Eres una API JSON que genera resúmenes detallados y extensos de series de televisión.
                    Para la serie "$title", proporciona un resumen que cubra la temporada $season, episodios $rangeStart a $rangeEnd.
                    Devuelve un JSON con esta estructura:
                    {
                        "summary": "el resumen aquí"
                    }

                    Pautas para el resumen:
                    - Escribe en español, adaptándote al estilo narrativo natural del idioma.
                    - Proporciona un resumen extenso, detallado y cohesionado, integrando los eventos en una narrativa fluida en lugar de listar episodios.
                    - Destaca los momentos clave de la trama, las interacciones significativas entre personajes y los desarrollos importantes de la historia.
                    - Describe las transformaciones o evoluciones de los personajes principales, con ejemplos concretos de sus decisiones o acciones.
                    - Menciona las subtramas relevantes y explica cómo se conectan con la trama principal o los personajes clave.
                    - Incluye el contexto y las motivaciones de los personajes, ilustrándolas con ejemplos específicos.
                    - Explica las relaciones entre personajes, detallando dinámicas de poder, alianzas, conflictos o vínculos románticos, según corresponda.
                    - Si es la primera mención de un personaje principal, incluye una breve descripción (ejemplo: "Ana, una detective astuta y reservada").
                    - Mantén un tono neutral, evitando lenguaje sensacionalista o emotivo.
                    - Evita spoilers importantes, como muertes de personajes principales o giros que alteren fundamentalmente la trama.
                    - Usa el tiempo presente.
                    - El resumen debe tener al menos $minWords palabras, ajustándose a la cantidad de episodios cubiertos.
                    - Concluye con una frase o pregunta intrigante que invite a ver los siguientes episodios, sin revelar spoilers.

                    IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.
                """.trimIndent()
            }
            "fr" -> if (isFromBeginning) {
                """
                    Tu es une API JSON qui génère des résumés détaillés et approfondis de séries télévisées.
                    Pour la série "$title", fournis un résumé couvrant de l'épisode 1 de la saison 1 à l'épisode $rangeEnd de la saison $season.
                    Retourne un JSON avec cette structure:
                    {
                        "summary": "le résumé ici"
                    }

                    Directives pour le résumé:
                    - Écris en français, en t'adaptant au style narratif naturel de la langue.
                    - Fournis un résumé détaillé, approfondi et cohérent, intégrant les événements dans une narration fluide plutôt que de lister les épisodes.
                    - Mets en avant les moments clés de l'intrigue, les interactions significatives entre personnages et les développements importants de l'histoire.
                    - Décris les transformations ou évolutions des personnages principaux, avec des exemples concrets de leurs décisions ou actions.
                    - Mentionne les sous-intrigues pertinentes et explique comment elles se connectent à l'intrigue principale ou aux personnages clés.
                    - Inclut le contexte et les motivations des personnages, illustrés par des exemples spécifiques.
                    - Explique les relations entre personnages, en détaillant les dynamiques de pouvoir, alliances, conflits ou liens romantiques, selon le cas.
                    - Pour la première mention d'un personnage principal, inclut une brève description (exemple : "Ana, une détective rusée et réservée").
                    - Maintiens un ton neutre, en évitant un langage sensationnaliste ou émotionnel.
                    - Évite les spoilers importants, comme les morts de personnages principaux ou les rebondissements qui altèrent fondamentalement l'intrigue.
                    - Utilise le présent.
                    - Le résumé doit comporter au moins $minWords mots, ajustés au nombre d'épisodes couverts.
                    - Conclus par une phrase ou une question intrigante qui incite à regarder les épisodes suivants, sans révéler de spoilers.

                    IMPORTANT: Retourne UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            } else {
                """
                    Tu es une API JSON qui génère des résumés détaillés et approfondis de séries télévisées.
                    Pour la série "$title", fournis un résumé couvrant la saison $season, épisodes $rangeStart à $rangeEnd.
                    Retourne un JSON avec cette structure:
                    {
                        "summary": "le résumé ici"
                    }

                    Directives pour le résumé:
                    - Écris en français, en t'adaptant au style narratif naturel de la langue.
                    - Fournis un résumé détaillé, approfondi et cohérent, intégrant les événements dans une narration fluide plutôt que de lister les épisodes.
                    - Mets en avant les moments clés de l'intrigue, les interactions significatives entre personnages et les développements importants de l'histoire.
                    - Décris les transformations ou évolutions des personnages principaux, avec des exemples concrets de leurs décisions ou actions.
                    - Mentionne les sous-intrigues pertinentes et explique comment elles se connectent à l'intrigue principale ou aux personnages clés.
                    - Inclut le contexte et les motivations des personnages, illustrés par des exemples spécifiques.
                    - Explique les relations entre personnages, en détaillant les dynamiques de pouvoir, alliances, conflits ou liens romantiques, selon le cas.
                    - Pour la première mention d'un personnage principal, inclut une brève description (exemple : "Ana, une détective rusée et réservée").
                    - Maintiens un ton neutre, en évitant un langage sensationnaliste ou émotionnel.
                    - Évite les spoilers importants, comme les morts de personnages principaux ou les rebondissements qui altèrent fondamentalement l'intrigue.
                    - Utilise le présent.
                    - Le résumé doit comporter au moins $minWords mots, ajustés au nombre d'épisodes couverts.
                    - Conclus par une phrase ou une question intrigante qui incite à regarder les épisodes suivants, sans révéler de spoilers.

                    IMPORTANT: Retourne UNIQUEMENT le JSON, sans texte supplémentaire.
                """.trimIndent()
            }
            "de" -> if (isFromBeginning) {
                """
                    Du bist eine JSON-API, die detaillierte und umfassende Zusammenfassungen von Fernsehserien generiert.
                    Für die Serie "$title" erstelle eine Zusammenfassung, die von Episode 1 der Staffel 1 bis Episode $rangeEnd der Staffel $season reicht.
                    Gib ein JSON mit dieser Struktur zurück:
                    {
                        "summary": "die Zusammenfassung hier"
                    }

                    Richtlinien für die Zusammenfassung:
                    - Schreibe auf Deutsch und passe dich dem natürlichen Erzählstil der Sprache an.
                    - Liefere eine detaillierte, umfassende und kohärente Zusammenfassung, die die Ereignisse in eine fließende Erzählung einbettet, anstatt Episoden aufzulisten.
                    - Hebe Schlüsselmomente der Handlung, signifikante Interaktionen zwischen Charakteren und wichtige Entwicklungen der Geschichte hervor.
                    - Beschreibe die Transformationen oder Entwicklungen der Hauptcharaktere mit konkreten Beispielen für ihre Entscheidungen oder Handlungen.
                    - Erwähne relevante Nebenhandlungen und erkläre, wie sie mit der Haupthandlung oder den Schlüsselfiguren verbunden sind.
                    - Füge den Kontext und die Motivationen der Charaktere hinzu, illustriert durch spezifische Beispiele.
                    - Erkläre die Beziehungen zwischen den Charakteren und gehe auf Machtdynamiken, Allianzen, Konflikte oder romantische Bindungen ein, falls zutreffend.
                    - Bei der ersten Erwähnung eines Hauptcharakters füge eine kurze Beschreibung hinzu (z. B. "Ana, eine schlaue und zurückhaltende Detektivin").
                    - Halte einen neutralen Ton bei und vermeide sensationalistische oder emotionale Sprache.
                    - Vermeide wichtige Spoiler, wie den Tod von Hauptcharakteren oder Wendungen, die die Handlung grundlegend verändern.
                    - Verwende die Gegenwart.
                    - Die Zusammenfassung muss mindestens $minWords Wörter umfassen, angepasst an die Anzahl der abgedeckten Episoden.
                    - Schließe mit einem fesselnden Satz oder einer Frage ab, die zum Anschauen der nächsten Episoden anregt, ohne Spoiler zu enthüllen.

                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            } else {
                """
                    Du bist eine JSON-API, die detaillierte und umfassende Zusammenfassungen von Fernsehserien generiert.
                    Für die Serie "$title" erstelle eine Zusammenfassung, die Staffel $season, Episoden $rangeStart bis $rangeEnd abdeckt.
                    Gib ein JSON mit dieser Struktur zurück:
                    {
                        "summary": "die Zusammenfassung hier"
                    }

                    Richtlinien für die Zusammenfassung:
                    - Schreibe auf Deutsch und passe dich dem natürlichen Erzählstil der Sprache an.
                    - Liefere eine detaillierte, umfassende und kohärente Zusammenfassung, die die Ereignisse in eine fließende Erzählung einbettet, anstatt Episoden aufzulisten.
                    - Hebe Schlüsselmomente der Handlung, signifikante Interaktionen zwischen Charakteren und wichtige Entwicklungen der Geschichte hervor.
                    - Beschreibe die Transformationen oder Entwicklungen der Hauptcharaktere mit konkreten Beispielen für ihre Entscheidungen oder Handlungen.
                    - Erwähne relevante Nebenhandlungen und erkläre, wie sie mit der Haupthandlung oder den Schlüsselfiguren verbunden sind.
                    - Füge den Kontext und die Motivationen der Charaktere hinzu, illustriert durch spezifische Beispiele.
                    - Erkläre die Beziehungen zwischen den Charakteren und gehe auf Machtdynamiken, Allianzen, Konflikte oder romantische Bindungen ein, falls zutreffend.
                    - Bei der ersten Erwähnung eines Hauptcharakters füge eine kurze Beschreibung hinzu (z. B. "Ana, eine schlaue und zurückhaltende Detektivin").
                    - Halte einen neutralen Ton bei und vermeide sensationalistische oder emotionale Sprache.
                    - Vermeide wichtige Spoiler, wie den Tod von Hauptcharakteren oder Wendungen, die die Handlung grundlegend verändern.
                    - Verwende die Gegenwart.
                    - Die Zusammenfassung muss mindestens $minWords Wörter umfassen, angepasst an die Anzahl der abgedeckten Episoden.
                    - Schließe mit einem fesselnden Satz oder einer Frage ab, die zum Anschauen der nächsten Episoden anregt, ohne Spoiler zu enthüllen.

                    WICHTIG: Gib NUR das JSON zurück, ohne zusätzlichen Text.
                """.trimIndent()
            }
            else -> if (isFromBeginning) {
                """
                    You are a JSON API that generates detailed and comprehensive summaries of TV series.
                    For the series "$title", provide a summary covering from episode 1 of season 1 to episode $rangeEnd of season $season.
                    Return a JSON with this structure:
                    {
                        "summary": "the summary here"
                    }

                    Guidelines for the summary:
                    - Write in English, adapting to the natural narrative style of the language.
                    - Provide a comprehensive, detailed, and cohesive summary, weaving events into a fluid narrative rather than listing episodes.
                    - Highlight key plot moments, significant character interactions, and major story developments.
                    - Describe the transformations or evolutions of main characters, with specific examples of their decisions or actions.
                    - Mention relevant subplots and explain how they connect to the main plot or key characters.
                    - Include context and character motivations, illustrated with specific examples.
                    - Explain character relationships, detailing power dynamics, alliances, conflicts, or romantic bonds as applicable.
                    - For the first mention of a main character, include a brief description (e.g., "Ana, a cunning and reserved detective").
                    - Maintain a neutral tone, avoiding sensationalist or emotional language.
                    - Avoid major spoilers, such as deaths of main characters or twists that fundamentally alter the plot.
                    - Use present tense.
                    - The summary must be at least $minWords words, adjusted to the number of episodes covered.
                    - Conclude with an intriguing statement or question that encourages viewing the next episodes, without spoilers.

                    IMPORTANT: Return ONLY the JSON, no additional text.
                """.trimIndent()
            } else {
                """
                    You are a JSON API that generates detailed and comprehensive summaries of TV series.
                    For the series "$title", provide a summary covering season $season, episodes $rangeStart to $rangeEnd.
                    Return a JSON with this structure:
                    {
                        "summary": "the summary here"
                    }

                    Guidelines for the summary:
                    - Write in English, adapting to the natural narrative style of the language.
                    - Provide a comprehensive, detailed, and cohesive summary, weaving events into a fluid narrative rather than listing episodes.
                    - Highlight key plot moments, significant character interactions, and major story developments.
                    - Describe the transformations or evolutions of main characters, with specific examples of their decisions or actions.
                    - Mention relevant subplots and explain how they connect to the main plot or key characters.
                    - Include context and character motivations, illustrated with specific examples.
                    - Explain character relationships, detailing power dynamics, alliances, conflicts, or romantic bonds as applicable.
                    - For the first mention of a main character, include a brief description (e.g., "Ana, a cunning and reserved detective").
                    - Maintain a neutral tone, avoiding sensationalist or emotional language.
                    - Avoid major spoilers, such as deaths of main characters or twists that fundamentally alter the plot.
                    - Use present tense.
                    - The summary must be at least $minWords words, adjusted to the number of episodes covered.
                    - Conclude with an intriguing statement or question that encourages viewing the next episodes, without spoilers.

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
    ): String {
        val mediaDetails = omdbService.getMediaDetails(imdbId = mediaId)
        val prompt = getPromptForLanguage(mediaDetails.Title, rangeStart, rangeEnd, season, isFromBeginning)
        
        val response = xaiService.getMediaSummary(
            PerplexityRequest(
                model = "grok-3-beta",
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
        
        return matchResult?.groupValues?.get(1)?.let { summary ->
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