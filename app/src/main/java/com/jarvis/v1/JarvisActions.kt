package com.jarvis.v1

data class JarvisCommandResult(
    val message: String,
    val needsConfirmation: Boolean = false
)

class JarvisActions {

    fun handle(command: JarvisCommand): JarvisCommandResult {
        return when (command.intent) {

            IntentType.CALL -> {
                if (command.target.isNullOrBlank()) {
                    JarvisCommandResult(
                        message = "Kisko call karna hai?"
                    )
                } else {
                    JarvisCommandResult(
                        message = "Theek hai, ${command.target} ko call karne ke liye ready hoon."
                    )
                }
            }

            IntentType.MESSAGE -> {
                JarvisCommandResult(
                    message = "Message samajh gaya. Bhejne se pehle confirmation lunga.",
                    needsConfirmation = true
                )
            }

            IntentType.OPEN_APP -> {
                JarvisCommandResult(
                    message = "App open karne ki request samajh gaya."
                )
            }

            IntentType.LAPTOP_COMMAND -> {
                JarvisCommandResult(
                    message = "Laptop control disabled hai."
                )
            }

            IntentType.GENERAL -> {
                JarvisCommandResult(
                    message = "Command ko AI brain ke through process karna hoga."
                )
            }
        }
    }
}
