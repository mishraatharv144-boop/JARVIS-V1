package com.jarvis.v1

import android.content.Context

class JarvisActions(
    private val context: Context
) {

    fun basicResponse(input: String): String {
        val text = input.trim()
        val lower = text.lowercase()

        return when {
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
                "Weather ke liye live weather data source connect karna hoga."

            lower.contains("bhopal") &&
            (
                lower.contains("jana") ||
                lower.contains("jaana") ||
                lower.contains("ghoom")
            ) ->
                "Bhopal travel request samajh gaya. Live weather aur route information connect hone ke baad current conditions check kar sakunga."

            lower.contains("lock") ||
            lower.contains("phone lock") ||
            lower.contains("फोन लॉक") ->
                "Phone lock command samajh gaya."

            lower.contains("message") ||
            lower.contains("msg") ||
            lower.contains("मैसेज") ->
                "Message request samajh gaya. Bhejne se pehle confirmation lunga."

            else ->
                "Samajh gaya: $text"
        }
    }
}
