package com.javhualde.nospoilerapk.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface XAIService {
    @POST("v1/chat/completions")
    suspend fun getMediaInfo(
        @Body request: PerplexityRequest,
        @Header("Authorization") authorization: String = "Bearer xai-L5VzxjJWJINDeNShmAtpJursKzLWuvlIFARfQtPt5SlUCR2Ea1JKFKS0kQuSKexBScVK6vWyenkuTuPy"
    ): PerplexityResponse

    @POST("v1/chat/completions")
    suspend fun getMediaSummary(
        @Body request: PerplexityRequest,
        @Header("Authorization") authorization: String = "Bearer xai-L5VzxjJWJINDeNShmAtpJursKzLWuvlIFARfQtPt5SlUCR2Ea1JKFKS0kQuSKexBScVK6vWyenkuTuPy"
    ): PerplexityResponse
}

data class PerplexityRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 1024
) 