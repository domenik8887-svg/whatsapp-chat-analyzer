package com.example.whatsappchatanalyzer

object ChatAnalyzer {
    private val stopWords = setOf(
        "und", "oder", "aber", "der", "die", "das", "ein", "eine",
        "ich", "du", "er", "sie", "es", "wir", "ihr", "nicht",
        "the", "a", "an", "to", "of", "for", "on", "at", "is", "are", "it", "you"
    )

    fun analyze(messages: List<ChatMessage>): ChatAnalysis {
        val messagesPerAuthor = messages.groupingBy { it.author }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

        val wordsPerAuthor = messages.groupBy { it.author }
            .mapValues { (_, msgs) ->
                msgs.sumOf { msg -> msg.text.split(Regex("\\s+")).count { w -> w.isNotBlank() } }
            }
            .toList()
            .sortedByDescending { it.second }

        val avgLengthPerAuthor = messages.groupBy { it.author }
            .mapValues { (_, msgs) ->
                val sizes = msgs.map { it.text.length }
                if (sizes.isEmpty()) 0.0 else sizes.average()
            }
            .toList()
            .sortedByDescending { it.second }

        val topWords = messages
            .flatMap { it.text.lowercase().split(Regex("[^\\p{L}\\p{N}]+")) }
            .asSequence()
            .filter { it.length >= 3 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(20)

        return ChatAnalysis(
            totalMessages = messages.size,
            messagesPerAuthor = messagesPerAuthor,
            wordsPerAuthor = wordsPerAuthor,
            avgLengthPerAuthor = avgLengthPerAuthor,
            topWords = topWords
        )
    }
}
