package com.javhualde.nospoilerapk.data.model

import com.google.gson.Gson

sealed interface MediaInfo {
    data class SeriesInfo(
        val totalSeasons: Int,
        val episodesPerSeason: Map<String, Int>
    ) : MediaInfo

    data class MovieInfo(
        val totalParts: Int,
        val parts: List<MoviePart>
    ) : MediaInfo

    data class MoviePart(
        val number: Int,
        val title: String,
        val year: String
    )

    companion object {
        fun fromJson(json: String, type: String): MediaInfo? {
            return try {
                println("Attempting to parse JSON: $json") // Debug log
                
                // Limpiar el JSON de marcadores markdown
                val cleanJson = json
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                
                println("Cleaned JSON: $cleanJson") // Debug log para ver el JSON limpio
                
                val gson = Gson()
                val result = when (type.lowercase()) {
                    "series" -> gson.fromJson(cleanJson, SeriesInfo::class.java)
                    else -> gson.fromJson(cleanJson, MovieInfo::class.java)
                }
                println("Successfully parsed JSON to: $result") // Debug log
                result
            } catch (e: Exception) {
                println("Error parsing JSON in MediaInfo: ${e.message}") // Debug log
                e.printStackTrace()
                null
            }
        }
    }
} 