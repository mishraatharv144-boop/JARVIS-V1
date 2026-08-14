package com.jarvis.v1

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var replyCallback: ((String) -> Unit)? = null

    private val brain by lazy {
        JarvisBrain(applicationContext)
    }

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val speechLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val text = result.data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                ?.firstOrNull()

            if (!text.isNullOrBlank()) {
                processCommand(text)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            10
        )

        setContent {
            JarvisScreen(
                onListen = {
                    startListening()
                },
                onReply = {
                    replyCallback = it
                }
            )
        }
    }

    private fun startListening() {

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Boliye..."
            )
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            speakAndDisplay(
                "Speech recognition is not available on this phone."
            )
        }
    }

    private fun processCommand(input: String) {

        replyCallback?.invoke("You: $input")

        activityScope.launch {

            val reply = brain.think(input)

            speakAndDisplay(reply)
        }
    }

    private fun speakAndDisplay(reply: String) {

        replyCallback?.invoke("JARVIS: $reply")

        tts?.speak(
            reply,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_reply"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts?.language = Locale(
                "hi",
                "IN"
            )
        }
    }

    override fun onDestroy() {

        activityScope.cancel()

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}

@Composable
fun JarvisScreen(
    onListen: () -> Unit,
    onReply: (((String) -> Unit)) -> Unit
) {

    var messages by remember {

        mutableStateOf(
            listOf(
                "JARVIS: V1 online. Tap the microphone and speak."
            )
        )
    }

    LaunchedEffect(Unit) {

        onReply { reply ->
            messages = messages + reply
        }
    }

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "J A R V I S",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "V1 • Advanced Assistant"
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    items(messages) { message ->

                        Text(
                            text = message,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Button(
                        onClick = onListen,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "🎙  TALK TO JARVIS"
                        )
                    }
                }
            }
        }
    }
}
