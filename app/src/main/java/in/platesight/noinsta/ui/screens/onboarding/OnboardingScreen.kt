package `in`.platesight.noinsta.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // If denied, we could check shouldShowRequestPermissionRationale here if we had the Activity
            // but for simplicity, we'll let the user click the button again or proceed.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            0 -> {
                Text("Step 1: Accessibility", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("We need accessibility permission to detect when Instagram is opened.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { showAccessibilityDisclosure = true }) {
                    Text("Grant Accessibility Permission")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { currentStep++ }) {
                    Text("Next")
                }
            }
            1 -> {
                Text("Step 2: Battery Optimization", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("To ensure NoInsta works reliably, please disable battery optimization.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Disable Battery Optimization")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { currentStep++ }) {
                    Text("Next")
                }
            }
            2 -> {
                Text("Step 3: Notifications", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("NoInsta uses a persistent notification to stay active.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Notifications work by default on older versions
                    }
                }) {
                    Text("Enable Notifications")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.onOnboardingComplete()
                    onComplete()
                }) {
                    Text("Finish")
                }
            }
        }
    }

    if (showAccessibilityDisclosure) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDisclosure = false },
            title = { Text("Accessibility Disclosure") },
            text = { Text("NoInsta uses Accessibility Services to monitor when the Instagram app is in the foreground. This data is used solely to help you manage your habit and is not shared with third parties.") },
            confirmButton = {
                TextButton(onClick = {
                    showAccessibilityDisclosure = false
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("I Understand")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDisclosure = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
