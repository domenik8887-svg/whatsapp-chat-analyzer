package com.example.whatsappchatanalyzer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AnalyzerApp(readFile = ::readFileFromUri)
            }
        }
    }

    private fun readFileFromUri(uri: Uri): String {
        contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
            return it.readText()
        }
        return ""
    }
}

@Composable
private fun AnalyzerApp(readFile: (Uri) -> String) {
    var analysis by remember { mutableStateOf<ChatAnalysis?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            val raw = readFile(uri)
            val messages = WhatsAppParser.parse(raw)
            if (messages.isEmpty()) {
                error = "Keine Nachrichten erkannt. Bitte WhatsApp-Export ohne Medien als .txt verwenden."
                analysis = null
            } else {
                analysis = ChatAnalyzer.analyze(messages)
                error = null
            }
        }.onFailure {
            error = "Fehler beim Lesen: ${it.message}"
            analysis = null
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WhatsApp Chat Analyzer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Wähle eine exportierte WhatsApp-Chatdatei (.txt) aus.")
            Button(onClick = { picker.launch(arrayOf("text/plain", "text/*")) }) {
                Text("Chatdatei auswählen")
            }

            error?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            analysis?.let { AnalysisView(it) }
        }
    }
}

@Composable
private fun AnalysisView(result: ChatAnalysis) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { InfoCard("Gesamtnachrichten", result.totalMessages.toString()) }

        item { Header("Nachrichten pro Person") }
        items(result.messagesPerAuthor) { (a, c) -> InfoCard(a, c.toString()) }

        item { Header("Wörter pro Person") }
        items(result.wordsPerAuthor) { (a, c) -> InfoCard(a, c.toString()) }

        item { Header("Ø Nachrichtenlänge") }
        items(result.avgLengthPerAuthor) { (a, c) -> InfoCard(a, "${"%.1f".format(c)} Zeichen") }

        item { Header("Top Wörter") }
        items(result.topWords) { (w, c) -> InfoCard(w, c.toString()) }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun Header(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun InfoCard(left: String, right: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(left)
            Text(right, fontWeight = FontWeight.Medium)
        }
    }
}
