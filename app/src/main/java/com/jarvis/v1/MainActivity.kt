package com.jarvis.v1

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.runtime.mutableStateListOf
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

    private lateinit var brain: JarvisBrain
    private lateinit var engine: JarvisEngine
    private lateinit var phoneActions: PhoneActions
    private lateinit var deviceLockManager: DeviceLockManager

    private lateinit var tts: TextToSpeech

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val messages = mutableStateListOf(
        "JARVIS: Online. Boliye."
    )

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListening()
            } else {
                reply("Microphone permission required hai.")
            }
        }

    private val contactsPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                reply("Contacts permission nahi mili.")
            }
        }

    private val speechLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val spokenText =
                result.data
                    ?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                    ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                messages.add("You: $spokenText")
                processCommand(spokenText)
            } else {
                reply("Mujhe kuch sunai nahi diya.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        brain = JarvisBrain(applicationContext)
        engine = JarvisEngine()
        phoneActions = PhoneActions(applicationContext)
        deviceLockManager = DeviceLockManager(applicationContext)

        tts = TextToSpeech(this, this)

        setContent {
            JarvisScreen(
                messages = messages,
                onListen = {
                    requestMicrophone()
                }
            )
        }
    }

    private fun requestMicrophone() {

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
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
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Boliye..."
            )
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            reply("Speech recognition available nahi hai.")
        }
    }

    private fun processCommand(input: String) {

        val lower = input.lowercase(Locale.getDefault())

        // PHONE LOCK
        if (
            lower.contains("phone lock") ||
            lower.contains("lock my phone") ||
            lower.contains("lock the phone") ||
            lower.contains("फोन लॉक") ||
            lower.contains("मोबाइल लॉक")
        ) {
            handlePhoneLock()
            return
        }

        scope.launch {

            val command = engine.understand(input)

            when (command.intent) {

                IntentType.CALL -> {
                    handleCall(command.target)
                }

                IntentType.MESSAGE -> {
                    reply(
                        "Message request samajh gaya. Bhejne se pehle confirmation lunga."
                    )
                }

                IntentType.OPEN_APP -> {
                    reply(
                        "App open karne ki request samajh gaya."
                    )
                }

                IntentType.LAPTOP_COMMAND -> {
                    reply(
                        "Laptop control disabled hai."
                    )
                }

                IntentType.GENERAL -> {
                    reply(brain.think(input))
                }
            }
        }
    }

    private fun handlePhoneLock() {

        if (deviceLockManager.isEnabled()) {

            reply("Phone lock kar raha hoon.")

            deviceLockManager.lockPhone()

        } else {

            reply(
                "Phone lock permission abhi enabled nahi hai. " +
                "Main Android ka Device Administrator setup khol raha hoon."
            )

            val component = ComponentName(
                this,
                JarvisDeviceAdminReceiver::class.java
            )

            val intent = Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
            ).apply {

                putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    component
                )

                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "JARVIS ko voice se supported phone-lock action perform karne ke liye access chahiye."
                )
            }

            startActivity(intent)
        }
    }

    private fun handleCall(target: String?) {

        if (target.isNullOrBlank()) {
            reply("Kisko call karna hai?")
            return
        }

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {

            contactsPermission.launch(
                Manifest.permission.READ_CONTACTS
            )

            reply(
                "Contacts permission allow karo, phir main contact search karunga."
            )

            return
        }

        val number = phoneActions.findContact(target)

        if (number == null) {

            reply(
                "$target naam ka contact nahi mila."
            )

            return
        }

        reply(
            "$target ka contact mil gaya. Dialer open kar raha hoon."
        )

        phoneActions.openDialer(number)
    }

    private fun reply(text: String) {

        messages.add("JARVIS: $text")

        if (::tts.isInitialized) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_response"
            )
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts.setLanguage(
                Locale("hi", "IN")
            )

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.language = Locale.US
            }
        }
    }

    override fun onDestroy() {

        scope.cancel()

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}

@Composable
private fun JarvisScreen(
    messages: List<String>,
    onListen: () -> Unit
) {

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
                    text = "Phone Assistant"
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(messages) { message ->

                        Text(
                            text = message,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
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
