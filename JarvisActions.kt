package com.jarvis.v1

data class JarvisCommand(
    val intent: IntentType,
    val target: String? = null,
    val requiresConfirmation: Boolean = false
)

enum class IntentType {
    CALL,
    MESSAGE,
    OPEN_APP,
    LAPTOP_COMMAND,
    GENERAL
}

class JarvisActions {

    fun understand(text: String): JarvisCommand {
        val input = text.lowercase().trim()

        return when {
            input.contains("call") ||
            input.contains("phone") ||
            input.contains("कॉल") ||
            input.contains("फोन") -> {

                JarvisCommand(
                    intent = IntentType.CALL,
                    target = extractTarget(input)
                )
            }

            input.contains("message") ||
            input.contains("msg") ||
            input.contains("मैसेज") -> {

                JarvisCommand(
                    intent = IntentType.MESSAGE,
                    target = extractTarget(input),
                    requiresConfirmation = true
                )
            }

            input.contains("laptop") ||
            input.contains("computer") ||
            input.contains("pc") -> {

                JarvisCommand(
                    intent = IntentType.LAPTOP_COMMAND,
                    target = input,
                    requiresConfirmation = true
                )
            }

            input.contains("open") ||
            input.contains("khol") ||
            input.contains("खोल") -> {

                JarvisCommand(
                    intent = IntentType.OPEN_APP,
                    target = extractTarget(input)
                )
            }

            else -> {
                JarvisCommand(
                    intent = IntentType.GENERAL
                )
            }
        }
    }

    private fun extractTarget(input: String): String? {

        val words = input
            .replace("please", "")
            .replace("please", "")
            .replace("call", "")
            .replace("phone", "")
            .replace("laga", "")
            .replace("lagao", "")
            .replace("karo", "")
            .replace("kar", "")
            .replace("ko", "")
            .replace("to", "")
            .trim()

        return words.ifBlank {
            null
        }
    }
}
