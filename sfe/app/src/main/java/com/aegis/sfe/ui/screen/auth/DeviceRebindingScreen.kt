package com.aegis.sfe.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.sfe.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRebindingScreen(
    username: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var aadhaarLast4 by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var motherMaidenName by remember { mutableStateOf("") }
    var firstSchoolName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Verification") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Identity Verification Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We detected a login attempt from a new device. Please verify your identity to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // User Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Username",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Divider()
            
            // Aadhaar Last 4 Digits
            OutlinedTextField(
                value = aadhaarLast4,
                onValueChange = { 
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        aadhaarLast4 = it
                    }
                },
                label = { Text("Last 4 digits of Aadhaar") },
                leadingIcon = {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = aadhaarLast4.isNotEmpty() && aadhaarLast4.length != 4,
                supportingText = {
                    if (aadhaarLast4.isNotEmpty() && aadhaarLast4.length != 4) {
                        Text("Must be exactly 4 digits")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // PAN Number
            OutlinedTextField(
                value = panNumber,
                onValueChange = { 
                    if (it.length <= 10) {
                        panNumber = it.uppercase()
                    }
                },
                label = { Text("PAN Number") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                placeholder = { Text("ABCDE1234F") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                isError = panNumber.isNotEmpty() && !isValidPAN(panNumber),
                supportingText = {
                    if (panNumber.isNotEmpty() && !isValidPAN(panNumber)) {
                        Text("Invalid PAN format")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider()
            
            Text(
                text = "Security Questions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            // Mother's Maiden Name
            OutlinedTextField(
                value = motherMaidenName,
                onValueChange = { motherMaidenName = it },
                label = { Text("Mother's Maiden Name") },
                leadingIcon = {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // First School Name
            OutlinedTextField(
                value = firstSchoolName,
                onValueChange = { firstSchoolName = it },
                label = { Text("First School Name") },
                leadingIcon = {
                    Icon(Icons.Default.School, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Error Message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Submit Button
            Button(
                onClick = {
                    if (validateInputs(aadhaarLast4, panNumber, motherMaidenName, firstSchoolName)) {
                        isProcessing = true
                        errorMessage = null
                        viewModel.performDeviceRebinding(
                            username = username,
                            aadhaarLast4 = aadhaarLast4,
                            panNumber = panNumber,
                            securityAnswers = mapOf(
                                "mother_maiden_name" to motherMaidenName,
                                "first_school" to firstSchoolName
                            ),
                            onSuccess = {
                                isProcessing = false
                                onSuccess()
                            },
                            onError = { error ->
                                isProcessing = false
                                errorMessage = error
                            }
                        )
                    } else {
                        errorMessage = "Please fill all fields correctly"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Verify Identity")
            }
            
            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
            
            // Security Notice
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your information is encrypted and transmitted securely. We will never ask for your full Aadhaar number or passwords.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

private fun isValidPAN(pan: String): Boolean {
    return pan.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
}

private fun validateInputs(
    aadhaarLast4: String,
    panNumber: String,
    motherMaidenName: String,
    firstSchoolName: String
): Boolean {
    return aadhaarLast4.length == 4 &&
           isValidPAN(panNumber) &&
           motherMaidenName.isNotBlank() &&
           firstSchoolName.isNotBlank()
}