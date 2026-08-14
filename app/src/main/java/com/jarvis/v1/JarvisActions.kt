package com.jarvis.v1

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JarvisBrain(
    private val context: Context
) {

    suspend fun think(input: String): String {
        return withContext(Dispatchers.Default) {

            val text = input.trim()
            val lower = text.lowercase()

            when {
                text.isBlank() ->
                    "Mujhe kuch sunai nahi diya."

                lower.contains("hello") ||
                lower.contains("hi") ||
                lower.contains("hey") ||
                lower.contains("namaste") ->
                    "Hello. Main JARVIS hoon. Batao, kya karna hai?"

                lower.contains("kaise ho") ||
                lower.contains("how are you") ->
                    "Main bilkul ready hoon. Tum batao kya karna hai."

                lower.contains("tumhara naam") ||
                lower.contains("your name") ->
                    "Mera naam JARVIS hai."

                lower.contains("thank") ||
                lower.contains("thanks") ||
                lower.contains("dhanyavaad") ->
                    "You're welcome."

                lower.contains("kya kar sakte ho") ||
                lower.contains("what can you do") ->
                    "Main voice commands, contacts, calling aur basic phone tasks me tumhari help kar sakta hoon."

                lower.contains("weather") ||
                lower.contains("mausam") ||
                lower.contains("baarish") ||
                lower.contains("barish") ->
                    "Weather ke liye mujhe live weather data source connect karna hoga. Abhi mere paas live weather access nahi hai."

                lower.contains("bhopal") &&
                (
                    lower.contains("jana") ||
                    lower.contains("jaana") ||
                    lower.contains("ghoom")
                ) ->
                    "Bhopal travel request samajh gaya. Live weather aur route information connect hone ke baad main current conditions check karke bata sakunga."

                lower.contains("lock") ||
                lower.contains("phone lock") ||
                lower.contains("फोन लॉक") ->
                    "Phone lock command samajh gaya. Is feature ke liye Android ka supported device-lock permission setup karna hoga."

                lower.contains("message") ||
                lower.contains("msg") ||
                lower.contains("मैसेज") ->
                    "Message request samajh gaya. Message bhejne se pehle confirmation lena zaroori hoga."

                lower.contains("call") ||
                lower.contains("phone") ||
                lower.contains("कॉल") ||
                lower.contains("फोन") ->
                    "Call request samajh gaya. Contact ka naam batao."

                else ->
                    "Samajh gaya: $text. Is request ke liye mujhe appropriate phone feature ya AI capability connect karni hogi."
            }
        }
    }
}
