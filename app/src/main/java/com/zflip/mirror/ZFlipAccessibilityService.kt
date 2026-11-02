package com.zflip.mirror

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class ZFlipAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: ZFlipAccessibilityService? = null

        fun openRecents(context: Context): Boolean {
            val service = instance
            return if (service != null) {
                service.performGlobalAction(GLOBAL_ACTION_RECENTS)
            } else {
                // Abre a tela de Acessibilidade para permitir que o usuário ative o serviço no Z Flip
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                false
            }
        }
    }
}
