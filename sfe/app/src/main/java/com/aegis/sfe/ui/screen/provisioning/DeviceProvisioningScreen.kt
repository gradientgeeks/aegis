package com.aegis.sfe.ui.screen.provisioning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.sfe.ui.viewmodel.DeviceProvisioningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceProvisioningScreen(
    onProvisioningComplete: () -> Unit,
    viewModel: DeviceProvisioningViewModel = viewModel()
) {
    val provisioningState by viewModel.provisioningState.collectAsState()
    // Security check is commented out - focusing on HMAC validation and key exchange
    // val securityCheckResult by viewModel.securityCheckResult.collectAsState()
    
    LaunchedEffect(provisioningState.isProvisioned) {
        if (provisioningState.isProvisioned) {
            onProvisioningComplete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UCO Bank - Setup") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // App Logo and Title
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "UCO Bank",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Secured by Aegis",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Security Status Card is commented out - focusing on HMAC validation and key exchange
            /*
            securityCheckResult?.let { security ->
                SecurityStatusCard(securityResult = security)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            */
            
            // Provisioning Status
            if (provisioningState.isProvisioned) {
                ProvisionedStatusCard(deviceId = provisioningState.deviceId)
            } else {
                NotProvisionedCard()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Provisioning Button
            if (!provisioningState.isProvisioned) {
                Button(
                    onClick = { viewModel.provisionDevice() },
                    enabled = !provisioningState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (provisioningState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Setting up device...")
                    } else {
                        Text("Setup Device Security")
                    }
                }
            }
            
            // Error handling
            provisioningState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Setup Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { viewModel.clearError() }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Information Text
            Text(
                text = "Device security setup is required to access banking features. " +
                        "This process establishes a secure connection with UCO Bank's servers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Security check is commented out - focusing on HMAC validation and key exchange
/*
@Composable
private fun SecurityStatusCard(securityResult: com.aegis.sfe.data.model.SecurityCheckResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (securityResult.isSecure) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            SecurityCheckItem("Device Security", securityResult.isSecure)
            SecurityCheckItem("Root Detection", !securityResult.rootDetected)
            SecurityCheckItem("Emulator Detection", !securityResult.emulatorDetected)
            SecurityCheckItem("Debug Mode", !securityResult.debugModeEnabled)
            
            if (securityResult.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Warnings:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                securityResult.warnings.forEach { warning ->
                    Text(
                        text = "• $warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
*/

// Security check is commented out - focusing on HMAC validation and key exchange
/*
@Composable
private fun SecurityCheckItem(label: String, isSecure: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = if (isSecure) "✓" else "⚠",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSecure) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
    }
}
*/

@Composable
private fun ProvisionedStatusCard(deviceId: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Device Ready",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your device has been securely configured for UCO Bank.",
                style = MaterialTheme.typography.bodyMedium
            )
            deviceId?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Device ID: ${it.take(12)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotProvisionedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Setup Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complete the security setup to access your UCO Bank account.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}