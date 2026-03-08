package com.example.whatsappchatanalyzer

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object WhatsAppParser {
    private val lineRegex = Regex("""^(\d{1,2}[./]\d{1,2}[./]\d{2,4}),\s(\d{1,2}:\d{2})(?:\s?(?:-\s)?)?(.*)$""")

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("d.M.yy H:mm"),
        DateTimeFormatter.ofPattern("d.M.yyyy H:mm"),
        DateTimeFormatter.ofPattern("d/M/yy H:mm"),
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm")
    )

    fun parse(raw: String): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        var currentDate: LocalDateTime? = null
        var currentAuthor = "System"
        val currentText = StringBuilder()

        fun flushCurrent() {
            val ts = currentDate ?: return
            val cleaned = currentText.toString().trim()
            if (cleaned.isNotBlank()) {
                messages += ChatMessage(ts, currentAuthor, cleaned)
            }
            currentText.clear()
        }

        raw.lineSequence().forEach { line ->
            val match = lineRegex.find(line)
            if (match != null) {
                flushCurrent()

                val date = match.groupValues[1]
                val time = match.groupValues[2]
                val payload = match.groupValues[3]
                currentDate = parseDateTime("$date $time") ?: currentDate

                val split = payload.split(": ", limit = 2)
                if (split.size == 2) {
                    currentAuthor = split[0].ifBlank { "Unknown" }
                    currentText.append(split[1])
                } else {
                    currentAuthor = "System"
                    currentText.append(payload)
                }
            } else if (currentText.isNotEmpty()) {
                currentText.append('\n').append(line)
            }
        }

        flushCurrent()
        return messages
    }

    private fun parseDateTime(input: String): LocalDateTime? {
        dateFormats.forEach { formatter ->
            try {
                return LocalDateTime.parse(input, formatter)
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }
}
