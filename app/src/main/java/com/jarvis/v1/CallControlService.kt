package com.jarvis.v1

import android.telecom.Call
import android.telecom.InCallService

class CallControlService : InCallService() {

    companion object {
        var currentCall: Call? = null
            private set
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
    }

    override fun onCallRemoved(call: Call) {
        if (currentCall == call) {
            currentCall = null
        }

        super.onCallRemoved(call)
    }

    fun answerCurrentCall() {
        currentCall?.answer(0)
    }

    fun rejectCurrentCall() {
        currentCall?.reject(false, null)
    }

    fun endCurrentCall() {
        currentCall?.disconnect()
    }
}
