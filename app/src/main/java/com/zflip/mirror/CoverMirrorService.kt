package com.zflip.mirror

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class CoverMirrorService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val keepCoverOn = intent?.getBooleanExtra(EXTRA_KEEP_COVER_ON, false) ?: false

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ZFlip Cover Mirror")
            .setContentText(if (keepCoverOn) "Espelhamento Ativo + Capa Ligada ao Abrir" else "Espelhamento ativo na tela de capa")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        if (keepCoverOn) {
            acquireWakeLock()
        } else {
            releaseWakeLock()
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "ZFlipMirrorApp:KeepCoverOnWakeLock"
                )
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(12 * 60 * 60 * 1000L)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ZFlip Cover Mirror Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "ZFlipMirrorChannel"
        private const val NOTIFICATION_ID = 2001
        private const val EXTRA_KEEP_COVER_ON = "extra_keep_cover_on"

        fun startMirroring(context: Context, keepCoverOn: Boolean = false) {
            try {
                val intent = Intent(context, CoverMirrorService::class.java).apply {
                    putExtra(EXTRA_KEEP_COVER_ON, keepCoverOn)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun stopMirroring(context: Context) {
            try {
                val intent = Intent(context, CoverMirrorService::class.java)
                context.stopService(intent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
