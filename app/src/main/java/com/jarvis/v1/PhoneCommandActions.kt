package com.jarvis.v1

import android.content.Context
import android.content.Intent
import android.net.Uri

class PhoneCommandActions(
    private val context: Context
) {

    fun openApp(packageName: String): Boolean {
        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(packageName)

        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }

    fun openSms(number: String?, message: String): Boolean {
        val uri = if (number.isNullOrBlank()) {
            Uri.parse("smsto:")
        } else {
            Uri.parse("smsto:${Uri.encode(number)}")
        }

        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
