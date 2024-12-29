package com.example.nospoilerapk.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PerplexityService {
    @POST("chat/completions")
    suspend fun getMediaInfo(
        @Body request: PerplexityRequest,
        @Header("Authorization") authorization: String = "Bearer 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"
    ): PerplexityResponse
}

data class PerplexityRequest(
    val model: String = "llama-3.1-sonar-small-128k-online",
    val messages: List<Message>,
    val max_tokens: Int = 1024
) 