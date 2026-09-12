package `in`.platesight.noinsta.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import `in`.platesight.noinsta.R
import timber.log.Timber

@AndroidEntryPoint
class NoInstaForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Starting foreground service")
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Service destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.d("Task removed, service will persist due to START_STICKY")
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setSmallIcon(R.mipmap.ic_launcher) // Using launcher icon for now
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "noinsta_foreground_service"

        fun start(context: Context) {
            val intent = Intent(context, NoInstaForegroundService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }
}
