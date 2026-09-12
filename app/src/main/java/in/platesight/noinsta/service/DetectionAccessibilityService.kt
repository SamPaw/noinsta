package `in`.platesight.noinsta.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import `in`.platesight.noinsta.domain.model.AppEventType
import `in`.platesight.noinsta.domain.usecase.ProcessAppEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class DetectionAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var processAppEventUseCase: ProcessAppEventUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var isInstagramForeground = false
    private var currentSessionId: String? = null

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                handleInstagramClosed()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                handlePackageChanged(packageName)
            }
            else -> {
                // Ignore other events
            }
        }
    }

    private fun handlePackageChanged(packageName: String) {
        val isInstagram = packageName == INSTAGRAM_PACKAGE

        if (isInstagram && !isInstagramForeground) {
            handleInstagramOpened()
        } else if (!isInstagram && isInstagramForeground) {
            handleInstagramClosed()
        }
    }

    private fun handleInstagramOpened() {
        Timber.d("Instagram opened")
        isInstagramForeground = true
        currentSessionId = UUID.randomUUID().toString()
        
        currentSessionId?.let { sessionId ->
            serviceScope.launch {
                processAppEventUseCase(AppEventType.INSTAGRAM_OPEN, sessionId)
            }
        }
    }

    private fun handleInstagramClosed() {
        if (!isInstagramForeground) return
        
        Timber.d("Instagram closed")
        isInstagramForeground = false
        
        currentSessionId?.let { sessionId ->
            serviceScope.launch {
                processAppEventUseCase(AppEventType.INSTAGRAM_CLOSE, sessionId)
            }
            currentSessionId = null
        }
    }

    override fun onInterrupt() {
        Timber.w("Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
    }

    companion object {
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    }
}
