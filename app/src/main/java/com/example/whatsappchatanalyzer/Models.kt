package com.example.whatsappchatanalyzer

import java.time.LocalDateTime

data class ChatMessage(
    val timestamp: LocalDateTime,
    val author: String,
    val text: String
)

data class ChatAnalysis(
    val totalMessages: Int,
    val messagesPerAuthor: List<Pair<String, Int>>,
    val wordsPerAuthor: List<Pair<String, Int>>,
    val avgLengthPerAuthor: List<Pair<String, Double>>,
    val topWords: List<Pair<String, Int>>
)
