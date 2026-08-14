package com.jarvis.v1

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!text.isNullOrBlank()) {
            sendReply(text)
        }
    }

    private var replyCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
        setContent {
            JarvisScreen(
                onListen = { startListening() },
                onReply = { replyCallback = it }
            )
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Boliye...")
        }
        try { speechLauncher.launch(intent) } catch (_: Exception) {
            sendReply("Speech recognition is not available on this phone.")
        }
    }

    private fun sendReply(input: String) {
        val reply = when {
            input.contains("hello", true) || input.contains("hi", true) ->
                "Hello! Main JARVIS hoon. Kaise help karun?"
            input.contains("jarvis", true) ->
                "Yes. I'm listening."
            else ->
                "Maine suna: $input. AI provider configure karne ke baad main intelligent answers de sakunga."
        }
        replyCallback?.invoke(reply)
        tts?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("hi", "IN")
    }

    override fun onDestroy() {
        tts?.stop(); tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun JarvisScreen(onListen: () -> Unit, onReply: (((String) -> Unit)) -> Unit) {
    var messages by remember { mutableStateOf(listOf("JARVIS V1 ready. Tap the microphone and say Hello JARVIS.")) }
    LaunchedEffect(Unit) {
        onReply { reply -> messages = messages + reply }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("J A R V I S", style = MaterialTheme.typography.headlineLarge)
                Text("V1 • Voice Assistant")
                Spacer(Modifier.height(20.dp))
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(messages) { Text(it, modifier = Modifier.padding(10.dp)) }
                }
                Button(onClick = onListen, modifier = Modifier.fillMaxWidth()) {
                    Text("🎙  TALK TO JARVIS")
                }
            }
        }
    }
}
