package com.jarvis.v1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var replyCallback: ((String) -> Unit)? = null

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var brain: JarvisBrain
    private lateinit var engine: JarvisEngine
    private lateinit var actions: JarvisActions
    private lateinit var phoneActions: PhoneActions
    private lateinit var confirmation: ConfirmationManager

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
            } else {
                speakAndShow("Mujhe kuch sunai nahi diya.")
            }
        }

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListeningInternal()
            } else {
                speakAndShow(
                    "Microphone permission ke bina main voice command nahi sun sakta."
                )
            }
        }

    private val contactsPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        brain = JarvisBrain(applicationContext)
        engine = JarvisEngine()
        actions = JarvisActions()
        phoneActions = PhoneActions(applicationContext)
        confirmation = ConfirmationManager()

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

        val permission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startListeningInternal()
        } else {
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startListeningInternal() {

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

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
            )
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            speakAndShow(
                "Is phone par speech recognition available nahi hai."
            )
        }
    }

    private fun processCommand(input: String) {

        replyCallback?.invoke("You: $input")

        scope.launch {

            val command = engine.understand(input)

            when (command.intent) {

                IntentType.CALL -> {

                    handleCall(command.target)
                }

                IntentType.MESSAGE -> {

                    speakAndShow(
                        "Message samajh gaya. Send karne se pehle confirmation lunga."
                    )
                }

                IntentType.OPEN_APP -> {

                    speakAndShow(
                        "App open karne ka request samajh gaya."
                    )
                }

                IntentType.LAPTOP_COMMAND -> {

                    speakAndShow(
                        "Laptop control abhi disabled hai."
                    )
                }

                IntentType.GENERAL -> {

                    val reply = brain.think(input)
                    speakAndShow(reply)
                }
            }
        }
    }

    private fun handleCall(target: String?) {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            contactsPermission.launch(
                Manifest.permission.READ_CONTACTS
            )

            speakAndShow(
                "Contacts access permission chahiye, phir main contact search kar sakta hoon."
            )

            return
        }

        if (target.isNullOrBlank()) {
            speakAndShow(
                "Kisko call karna hai?"
            )
            return
        }

        val number = phoneActions.findContact(target)

        if (number != null) {

            speakAndShow(
                "Mujhe $target ka contact mil gaya. Dialer open kar raha hoon."
            )

            phoneActions.openDialer(number)

        } else {

            speakAndShow(
                "$target naam ka contact nahi mila."
            )
        }
    }

    private fun speakAndShow(text: String) {

        replyCallback?.invoke("JARVIS: $text")

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_reply"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(
                Locale("hi", "IN")
            )

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts?.language = Locale.US
            }
        }
    }

    override fun onDestroy() {

        scope.cancel()

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
                "JARVIS: Online. Tap the microphone and speak."
            )
        )
    }

    LaunchedEffect(Unit) {

        onReply { message ->
            messages = messages + message
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

                Text(
                    text = "Advanced Voice Assistant"
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

                Button(
                    onClick = onListen,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("🎙 TALK TO JARVIS")
                }
            }
        }
    }
}
