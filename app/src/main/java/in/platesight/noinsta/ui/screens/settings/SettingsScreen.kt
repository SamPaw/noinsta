package `in`.platesight.noinsta.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val COOLDOWN_PRESETS = listOf(
    0 to "Disabled (0m - Intervene every time)",
    60 to "1 minute",
    120 to "2 minutes",
    300 to "5 minutes (Recommended)",
    600 to "10 minutes",
    900 to "15 minutes",
    1800 to "30 minutes"
)

private fun formatCooldownDisplay(seconds: Int): String {
    return when (seconds) {
        0 -> "Disabled (0m)"
        else -> "${seconds / 60} min"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onUnpair: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPrivacyDisclosure by remember { mutableStateOf(false) }
    var showCooldownDialog by remember { mutableStateOf(false) }
    var selectedCooldown by remember { mutableIntStateOf(uiState.cooldownSeconds) }

    LaunchedEffect(uiState.cooldownSeconds) {
        selectedCooldown = uiState.cooldownSeconds
    }

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Cooldown Configuration Section
            Text("Intervention Rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCooldownDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intervention Cooldown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Minimum delay before consecutive Instagram opens trigger a new laptop intervention.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        TextButton(onClick = { showCooldownDialog = true }) {
                            Text(formatCooldownDisplay(uiState.cooldownSeconds), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (uiState.statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.statusMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Diagnostics Section
            Text("Diagnostics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Server Status", style = MaterialTheme.typography.titleSmall)
                        Text(uiState.healthStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Button(
                        onClick = { viewModel.checkHealth() },
                        enabled = !uiState.isCheckingHealth,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ping")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Section
            Text("Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showPrivacyDisclosure = true }) {
                Text("Privacy & Telemetry Disclosure")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Unpair Device
            Button(
                onClick = { viewModel.unpair(onUnpair) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Unpair Device")
            }
        }
    }

    // Cooldown Picker Dialog
    if (showCooldownDialog) {
        AlertDialog(
            onDismissRequest = { showCooldownDialog = false },
            title = { Text("Set Intervention Cooldown") },
            text = {
                Column {
                    Text(
                        text = "Choose how long NoInsta waits after an intervention before firing another on your laptop:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    COOLDOWN_PRESETS.forEach { (seconds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCooldown = seconds }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedCooldown == seconds),
                                onClick = { selectedCooldown = seconds }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateCooldown(selectedCooldown)
                    showCooldownDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCooldownDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy Disclosure Dialog
    if (showPrivacyDisclosure) {
        AlertDialog(
            onDismissRequest = { showPrivacyDisclosure = false },
            title = { Text("Privacy & Telemetry Disclosure") },
            text = {
                Text("NoInsta records app usage timestamps and active sessions for Instagram to deliver habit-breaking interventions and personal analytics. All telemetry is stored securely on your server.")
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDisclosure = false }) {
                    Text("Close")
                }
            }
        )
    }
}
