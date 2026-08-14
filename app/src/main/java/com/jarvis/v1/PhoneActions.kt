package com.jarvis.v1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract

class PhoneActions(
    private val context: Context
) {

    fun findContact(name: String): String? {
        val cursor = context.contentResolver.query(
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
                val number = it.getString(
                    it.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
                return number
            }
        }

        return null
    }

    fun openDialer(number: String) {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:${Uri.encode(number)}")
        )

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
