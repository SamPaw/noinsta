package `in`.platesight.noinsta.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NoInsta Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device: ${uiState.deviceName}", style = MaterialTheme.typography.titleMedium)
                    Text("ID: ${uiState.deviceId}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: Monitoring Active",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Statistics", style = MaterialTheme.typography.headlineSmall)
            Text(uiState.stats)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.triggerTestEvent() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Trigger Now")
            }
        }
    }
}
