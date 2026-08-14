package com.jarvis.v1

class JarvisEngine {

    data class Result(
        val reply: String,
        val action: Action = Action.NONE,
        val requiresConfirmation: Boolean = false
    )

    enum class Action {
        NONE,
        CALL_CONTACT,
        SEND_MESSAGE,
        OPEN_APP,
        LAPTOP_COMMAND
    }

    fun process(input: String): Result {
        val text = input.trim().lowercase()

        if (text.isBlank()) {
            return Result("Mujhe kuch sunai nahi diya.")
        }

        if (
            text.contains("hello") ||
            text.contains("hi") ||
            text.contains("hey")
        ) {
            return Result(
                "Hello. Main JARVIS hoon. Batao kya karna hai?"
            )
        }

        if (
            text.contains("call") ||
            text.contains("phone laga") ||
            text.contains("फोन लगा") ||
            text.contains("कॉल")
        ) {
            return Result(
                reply = "Theek hai. Kis contact ko call karna hai?",
                action = Action.CALL_CONTACT
            )
        }

        if (
            text.contains("message") ||
            text.contains("msg") ||
            text.contains("message bhej") ||
            text.contains("मैसेज")
        ) {
            return Result(
                reply = "Message action samajh gaya. Send karne se pehle main confirmation lunga.",
                action = Action.SEND_MESSAGE,
                requiresConfirmation = true
            )
        }

        if (
            text.contains("laptop") ||
            text.contains("computer") ||
            text.contains("pc")
        ) {
            return Result(
                reply = "Laptop command samajh gaya. Secure laptop connection configure hone ke baad main action perform karunga.",
                action = Action.LAPTOP_COMMAND,
                requiresConfirmation = true
            )
        }

        if (
            text.contains("open") ||
            text.contains("khol") ||
            text.contains("खोल")
        ) {
            return Result(
                reply = "App open karne ki request samajh gaya.",
                action = Action.OPEN_APP
            )
        }

        if (
            text.contains("kaise ho") ||
            text.contains("how are you")
        ) {
            return Result(
                "Main ready hoon. Tumhari command ka wait kar raha hoon."
            )
        }

        return Result(
            reply = "Samajh gaya: $input. Is command ko AI brain ke through interpret karne ke liye next layer connect karenge."
        )
    }
}
