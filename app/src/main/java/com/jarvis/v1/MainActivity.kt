package com.jarvis.v1

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var brain: JarvisBrain
    private lateinit var engine: JarvisEngine
    private lateinit var phoneActions: PhoneActions
    private lateinit var commandActions: PhoneCommandActions
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
            if (granted) startListening()
            else reply("Microphone permission required hai.")
        }

    private val contactsPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                reply("Contacts permission mil gayi.")
            } else {
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
        commandActions = PhoneCommandActions(applicationContext)
        deviceLockManager = DeviceLockManager(applicationContext)

        tts = TextToSpeech(this, this)

        setContent {
            JarvisScreen(
                messages = messages,
                onListen = { requestMicrophone() }
            )
        }
    }

    private fun requestMicrophone() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startListening() {

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

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

        val text = input.lowercase(Locale.getDefault())

        when {

            // PHONE LOCK
            text.contains("phone lock") ||
            text.contains("lock my phone") ||
            text.contains("lock the phone") ||
            text.contains("फोन लॉक") ||
            text.contains("मोबाइल लॉक") -> {
                handlePhoneLock()
            }

            // ANSWER CALL
            text.contains("answer call") ||
            text.contains("receive call") ||
            text.contains("call receive") ||
            text.contains("कॉल उठा") ||
            text.contains("कॉल रिसीव") -> {

                if (CallControlService.answerCall()) {
                    reply("Call receive kar diya.")
                } else {
                    reply(
                        "Abhi active call control available nahi hai."
                    )
                }
            }

            // REJECT CALL
            text.contains("reject call") ||
            text.contains("decline call") ||
            text.contains("cut the call") ||
            text.contains("कॉल काट") ||
            text.contains("कॉल कट") -> {

                if (CallControlService.rejectCall()) {
                    reply("Call reject kar diya.")
                } else {
                    reply(
                        "Abhi active call control available nahi hai."
                    )
                }
            }

            // END ACTIVE CALL
            text.contains("end call") ||
            text.contains("hang up") ||
            text.contains("फोन काट") -> {

                if (CallControlService.endCall()) {
                    reply("Call end kar diya.")
                } else {
                    reply(
                        "Abhi koi controllable active call nahi hai."
                    )
                }
            }

            // OPEN COMMON APPS
            text.contains("open whatsapp") ||
            text.contains("whatsapp kholo") -> {

                openApp(
                    "com.whatsapp",
                    "WhatsApp"
                )
            }

            text.contains("open youtube") ||
            text.contains("youtube kholo") -> {

                openApp(
                    "com.google.android.youtube",
                    "YouTube"
                )
            }

            text.contains("open instagram") ||
            text.contains("instagram kholo") -> {

                openApp(
                    "com.instagram.android",
                    "Instagram"
                )
            }

            // SMS
            text.startsWith("sms ") ||
            text.startsWith("message ") ||
            text.contains("sms bhejo") ||
            text.contains("message bhejo") ||
            text.contains("मैसेज भेज") -> {

                reply(
                    "Message request samajh gaya. " +
                    "Actual message bhejne se pehle confirmation zaroori hai."
                )
            }

            // NORMAL JARVIS ENGINE
            else -> {

                scope.launch {

                    val command =
                        engine.understand(input)

                    when (command.intent) {

                        IntentType.CALL ->
                            handleCall(command.target)

                        IntentType.MESSAGE ->
                            reply(
                                "Message request samajh gaya. " +
                                "Bhejne se pehle confirmation lunga."
                            )

                        IntentType.OPEN_APP ->
                            reply(
                                "App open karne ke liye exact app name batao."
                            )

                        IntentType.LAPTOP_COMMAND ->
                            reply(
                                "Laptop control disabled hai."
                            )

                        IntentType.GENERAL ->
                            reply(
                                brain.think(input)
                            )
                    }
                }
            }
        }
    }

    private fun openApp(
        packageName: String,
        appName: String
    ) {

        if (commandActions.openApp(packageName)) {
            reply("$appName open kar raha hoon.")
        } else {
            reply(
                "$appName phone me installed nahi hai."
            )
        }
    }

    private fun handlePhoneLock() {

        if (deviceLockManager.isEnabled()) {

            reply("Phone lock kar raha hoon.")

            deviceLockManager.lockPhone()

        } else {

            reply(
                "Phone-lock permission enabled nahi hai. " +
                "Android ka setup screen open kar raha hoon."
            )

            val component =
                ComponentName(
                    this,
                    JarvisDeviceAdminReceiver::class.java
                )

            val intent =
                Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                ).apply {

                    putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        component
                    )

                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "JARVIS ko supported phone-lock action ke liye access chahiye."
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

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            contactsPermission.launch(
                Manifest.permission.READ_CONTACTS
            )
            return
        }

        val number =
            phoneActions.findContact(target)

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

            val result =
                tts.setLanguage(
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

                Text("Phone Assistant")

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
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
