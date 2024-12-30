package com.javhualde.nospoilerapk.data.network

data class PerplexityResponse(
    val id: String,
    val model: String,
    val created: Long,
    val choices: List<Choice>,
    val citations: List<String>? = null,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Message(
    val role: String,
    val content: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
) 