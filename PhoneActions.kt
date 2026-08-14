package com.jarvis.v1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract

class PhoneActions(
    private val context: Context
) {

    fun findContact(name: String): String? {
        val resolver = context.contentResolver

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val number = it.getString(1)
                return number
            }
        }

        return null
    }

    fun openDialer(number: String) {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:$number")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }

    fun openApp(packageName: String): Boolean {
        val intent =
            context.packageManager.getLaunchIntentForPackage(packageName)

        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } else {
            false
        }
    }
}
