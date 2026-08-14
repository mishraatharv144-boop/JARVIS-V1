package com.jarvis.v1

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

class DeviceLockManager(
    private val context: Context
) {

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE)
                as DevicePolicyManager

    private val adminComponent =
        ComponentName(
            context,
            JarvisDeviceAdminReceiver::class.java
        )

    fun isEnabled(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    fun lockPhone(): Boolean {
        if (!isEnabled()) {
            return false
        }

        devicePolicyManager.lockNow()
        return true
    }
}
