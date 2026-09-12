package `in`.platesight.noinsta.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onUnpair: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPrivacyDisclosure by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Diagnostics", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Health Status: ${uiState.healthStatus}")
                TextButton(onClick = { viewModel.checkHealth() }, enabled = !uiState.isCheckingHealth) {
                    Text("Ping")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Legal", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showPrivacyDisclosure = true }) {
                Text("Privacy Disclosure")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.unpair(onUnpair) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Unpair Device")
            }
        }
    }

    if (showPrivacyDisclosure) {
        AlertDialog(
            onDismissRequest = { showPrivacyDisclosure = false },
            title = { Text("Privacy Disclosure") },
            text = { Text("NoInsta collects app usage events (open/close) for Instagram to provide habit tracking. All data is securely transmitted and you can delete your data by unpairing this device.") },
            confirmButton = {
                TextButton(onClick = { showPrivacyDisclosure = false }) {
                    Text("Close")
                }
            }
        )
    }
}
