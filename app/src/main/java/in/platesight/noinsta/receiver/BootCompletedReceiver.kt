package `in`.platesight.noinsta.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import `in`.platesight.noinsta.service.NoInstaForegroundService
import timber.log.Timber

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Timber.d("Boot or package update completed, starting foreground service")
            context?.let {
                NoInstaForegroundService.start(it)
            }
        }
    }
}
