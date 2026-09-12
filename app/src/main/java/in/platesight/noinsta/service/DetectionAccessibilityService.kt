package `in`.platesight.noinsta.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetectionAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
