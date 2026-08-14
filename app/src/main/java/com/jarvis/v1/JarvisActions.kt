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
                        "Kisko call karna hai?"
                    )
                } else {
                    JarvisCommandResult(
                        "Theek hai, ${command.target} ko call karne ke liye ready hoon."
                    )
                }
            }

            IntentType.MESSAGE -> {
                JarvisCommandResult(
                    "Message samajh gaya. Bhejne se pehle confirmation lunga.",
                    needsConfirmation = true
                )
            }

            IntentType.OPEN_APP -> {
                JarvisCommandResult(
                    "App open karne ki request samajh gaya."
                )
            }

            IntentType.LAPTOP_COMMAND -> {
