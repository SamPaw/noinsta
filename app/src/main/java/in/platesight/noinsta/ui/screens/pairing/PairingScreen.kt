package `in`.platesight.noinsta.ui.screens.pairing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PairingScreen(
    viewModel: PairingViewModel = hiltViewModel(),
    onPairingSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pair Device", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter the 6-digit code shown on your pairing dashboard.")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = uiState.pairingCode,
            onValueChange = { if (it.length <= 6) viewModel.onPairingCodeChanged(it) },
            label = { Text("Pairing Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.deviceName,
            onValueChange = { viewModel.onDeviceNameChanged(it) },
            label = { Text("Device Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (uiState.error != null) {
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Button(
            onClick = { viewModel.claimPairing(onPairingSuccess) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.pairingCode.length == 6 && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Pair Device")
            }
        }
    }
}
