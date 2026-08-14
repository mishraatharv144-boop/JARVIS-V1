package com.jarvis.v1

import android.telecom.Call
import android.telecom.InCallService

class CallControlService : InCallService() {

    companion object {
        private var activeCall: Call? = null

        fun hasActiveCall(): Boolean {
            return activeCall != null
        }

        fun answerCall(): Boolean {
            val call = activeCall ?: return false

            return try {
                call.answer(0)
                true
            } catch (_: Exception) {
                false
            }
        }

        fun rejectCall(): Boolean {
            val call = activeCall ?: return false

            return try {
                call.reject(false, null)
                true
            } catch (_: Exception) {
                false
            }
        }

        fun endCall(): Boolean {
            val call = activeCall ?: return false

            return try {
                call.disconnect()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        activeCall = call
    }

    override fun onCallRemoved(call: Call) {
        if (activeCall == call) {
            activeCall = null
        }

        super.onCallRemoved(call)
    }
}
