package com.jarvis.v1

class ConfirmationManager {

    private var pendingAction: String? = null

    fun requestConfirmation(action: String): String {
        pendingAction = action
        return "Ye important action hai: $action. Kya main proceed karun?"
    }

    fun confirm(): Boolean {
        if (pendingAction == null) return false

        pendingAction = null
        return true
    }

    fun cancel(): Boolean {
        if (pendingAction == null) return false

        pendingAction = null
        return true
    }

    fun hasPendingAction(): Boolean {
        return pendingAction != null
    }
}
