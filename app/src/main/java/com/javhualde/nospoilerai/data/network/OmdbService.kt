package com.javhualde.nospoilerapk.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbService {
    @GET("/")
    suspend fun searchMedia(
        @Query("apikey") apiKey: String = "b56c1e19",
        @Query("s") searchQuery: String
    ): SearchResponse

    @GET("/")
    suspend fun getMediaDetails(
        @Query("apikey") apiKey: String = "b56c1e19",
        @Query("i") imdbId: String,
        @Query("plot") plot: String = "full",
        @Query("r") format: String = "json"
    ): DetailedMediaItem
}

data class SearchResponse(
    val Search: List<MediaItem>?,
    val totalResults: String?,
    val Response: String,
    val Error: String?
)

data class MediaItem(
    val Title: String,
    val Year: String,
    val imdbID: String,
    val Type: String,
    val Poster: String
)

data class DetailedMediaItem(
    val Title: String,
    val Year: String,
    val Rated: String,
    val Released: String,
    val Runtime: String,
    val Genre: String,
    val Director: String,
    val Writer: String,
    val Actors: String,
    val Plot: String,
    val Language: String,
    val Country: String,
    val Awards: String,
    val Poster: String,
    val Ratings: List<Rating>,
    val Metascore: String,
    val imdbRating: String,
    val imdbVotes: String,
    val imdbID: String,
    val Type: String,
    val totalSeasons: String?,
    val Response: String,
    val Season: String?,
    val Episodes: String?
)

data class Rating(
    val Source: String,
    val Value: String
) 