package com.jarvis.v1

data class JarvisCommand(
    val intent: IntentType,
    val target: String? = null
)

enum class IntentType {
    CALL,
    MESSAGE,
    OPEN_APP,
    LAPTOP_COMMAND,
    GENERAL
}

class JarvisEngine {

    fun understand(input: String): JarvisCommand {
        val text = input.trim().lowercase()

        return when {
            text.contains("call") ||
            text.contains("phone") ||
            text.contains("कॉल") ||
            text.contains("फोन") -> {
                JarvisCommand(
                    intent = IntentType.CALL,
                    target = extractTarget(text)
                )
            }

            text.contains("message") ||
            text.contains("msg") ||
            text.contains("मैसेज") -> {
                JarvisCommand(
                    intent = IntentType.MESSAGE,
                    target = extractTarget(text)
                )
            }

            text.contains("open") ||
            text.contains("khol") ||
            text.contains("खोल") -> {
                JarvisCommand(
                    intent = IntentType.OPEN_APP,
                    target = extractTarget(text)
                )
            }

            text.contains("laptop") ||
            text.contains("computer") ||
            text.contains("pc") -> {
                JarvisCommand(
                    intent = IntentType.LAPTOP_COMMAND,
                    target = text
                )
            }

            else -> {
                JarvisCommand(
                    intent = IntentType.GENERAL
                )
            }
        }
    }

    private fun extractTarget(text: String): String? {
        val cleaned = text
            .replace("please", "")
            .replace("call", "")
            .replace("phone", "")
            .replace("laga", "")
            .replace("lagao", "")
            .replace("karo", "")
            .replace("kar", "")
            .replace("ko", "")
            .replace("message", "")
            .replace("msg", "")
            .replace("open", "")
            .replace("khol", "")
            .replace("खोल", "")
            .replace("कॉल", "")
            .replace("फोन", "")
            .replace("मैसेज", "")
            .trim()

        return cleaned.ifBlank { null }
    }
}
