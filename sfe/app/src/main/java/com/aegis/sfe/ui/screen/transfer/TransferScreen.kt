package com.aegis.sfe.ui.screen.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.sfe.data.model.TransferRequest
import com.aegis.sfe.ui.viewmodel.BankingViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onNavigateBack: () -> Unit,
    onTransferComplete: () -> Unit,
    viewModel: BankingViewModel = viewModel()
) {
    val userAccounts by viewModel.userAccounts.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    
    var fromAccount by remember { mutableStateOf("") }
    var toAccount by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isToAccountValid by remember { mutableStateOf(false) }
    var showValidation by remember { mutableStateOf(false) }
    var useEncryption by remember { mutableStateOf(true) } // Default to encrypted transfer
    
    // Handle transfer success
    LaunchedEffect(transferState.success) {
        if (transferState.success != null) {
            onTransferComplete()
        }
    }
    
    // Validate to account when it changes
    LaunchedEffect(toAccount) {
        if (toAccount.length == 12) {
            viewModel.validateAccount(toAccount) { isValid ->
                isToAccountValid = isValid
            }
        } else {
            isToAccountValid = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            
            // Security indicator
            SecurityIndicatorCard()
            
            // From Account Selection
            FromAccountSection(
                accounts = userAccounts,
                selectedAccount = fromAccount,
                onAccountSelected = { fromAccount = it }
            )
            
            // To Account Input
            ToAccountSection(
                toAccount = toAccount,
                onToAccountChanged = { toAccount = it },
                isValid = isToAccountValid,
                showValidation = showValidation && toAccount.isNotEmpty()
            )
            
            // Amount Input
            AmountSection(
                amount = amount,
                onAmountChanged = { amount = it },
                fromAccount = userAccounts.find { it.accountNumber == fromAccount }
            )
            
            // Description Input
            DescriptionSection(
                description = description,
                onDescriptionChanged = { description = it },
                remarks = remarks,
                onRemarksChanged = { remarks = it }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Encryption Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "End-to-End Encryption",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Encrypt transfer data using session keys",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = useEncryption,
                        onCheckedChange = { useEncryption = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Transfer Button
            Button(
                onClick = {
                    showValidation = true
                    if (validateTransferForm(fromAccount, toAccount, amount, isToAccountValid)) {
                        val transferRequest = TransferRequest(
                            fromAccount = fromAccount,
                            toAccount = toAccount,
                            amount = BigDecimal(amount),
                            currency = "INR",
                            description = description.ifEmpty { null },
                            remarks = remarks.ifEmpty { null }
                        )
                        if (useEncryption) {
                            viewModel.transferMoneySecure(transferRequest)
                        } else {
                            viewModel.transferMoney(transferRequest)
                        }
                    }
                },
                enabled = !transferState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (transferState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing Transfer...")
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transfer Money")
                }
            }
            
            // Error handling
            transferState.error?.let { error ->
                ErrorCard(
                    error = error,
                    onDismiss = { viewModel.clearTransferState() }
                )
            }
            
            // Form validation errors
            if (showValidation) {
                FormValidationErrors(
                    fromAccount = fromAccount,
                    toAccount = toAccount,
                    amount = amount,
                    isToAccountValid = isToAccountValid
                )
            }
        }
    }
}

@Composable
private fun SecurityIndicatorCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Secure Transfer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "This transfer will be cryptographically signed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FromAccountSection(
    accounts: List<com.aegis.sfe.data.model.Account>,
    selectedAccount: String,
    onAccountSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Column {
        Text(
            text = "From Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = if (selectedAccount.isEmpty()) "Select Account" else {
                    val account = accounts.find { it.accountNumber == selectedAccount }
                    "${account?.accountNumber} - ${account?.accountType?.name?.replace("_", " ")} (${currencyFormat.format(account?.balance)})"
                },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "${account.accountNumber} - ${account.accountType.name.replace("_", " ")}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Balance: ${currencyFormat.format(account.balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onAccountSelected(account.accountNumber)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToAccountSection(
    toAccount: String,
    onToAccountChanged: (String) -> Unit,
    isValid: Boolean,
    showValidation: Boolean
) {
    Column {
        Text(
            text = "To Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = toAccount,
            onValueChange = onToAccountChanged,
            label = { Text("Account Number") },
            placeholder = { Text("Enter 12-digit account number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                if (toAccount.length == 12) {
                    Icon(
                        imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = if (isValid) "Valid" else "Invalid",
                        tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = showValidation && toAccount.isNotEmpty() && (!isValid || toAccount.length != 12),
            supportingText = {
                if (showValidation && toAccount.isNotEmpty()) {
                    when {
                        toAccount.length != 12 -> Text("Account number must be 12 digits")
                        !isValid -> Text("Account not found or invalid")
                        isValid -> Text("Account verified", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChanged: (String) -> Unit,
    fromAccount: com.aegis.sfe.data.model.Account?
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Column {
        Text(
            text = "Amount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    onAmountChanged(newValue)
                }
            },
            label = { Text("Amount (INR)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            supportingText = {
                fromAccount?.let { account ->
                    Text("Available: ${currencyFormat.format(account.balance)}")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    remarks: String,
    onRemarksChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Description (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("Description") },
            placeholder = { Text("Payment description") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = remarks,
            onValueChange = onRemarksChanged,
            label = { Text("Remarks") },
            placeholder = { Text("Additional remarks") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
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
                    text = "Transfer Failed",
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
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun FormValidationErrors(
    fromAccount: String,
    toAccount: String,
    amount: String,
    isToAccountValid: Boolean
) {
    val errors = mutableListOf<String>()
    
    if (fromAccount.isEmpty()) errors.add("Please select a from account")
    if (toAccount.isEmpty()) errors.add("Please enter to account number")
    if (toAccount.isNotEmpty() && toAccount.length != 12) errors.add("Account number must be 12 digits")
    if (toAccount.length == 12 && !isToAccountValid) errors.add("To account is invalid")
    if (amount.isEmpty()) errors.add("Please enter amount")
    if (amount.isNotEmpty()) {
        try {
            val amountValue = BigDecimal(amount)
            if (amountValue <= BigDecimal.ZERO) errors.add("Amount must be greater than 0")
        } catch (e: NumberFormatException) {
            errors.add("Invalid amount format")
        }
    }
    
    if (errors.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Please fix the following errors:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                errors.forEach { error ->
                    Text(
                        text = "• $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

private fun validateTransferForm(
    fromAccount: String,
    toAccount: String,
    amount: String,
    isToAccountValid: Boolean
): Boolean {
    if (fromAccount.isEmpty()) return false
    if (toAccount.isEmpty() || toAccount.length != 12 || !isToAccountValid) return false
    if (amount.isEmpty()) return false
    
    try {
        val amountValue = BigDecimal(amount)
        if (amountValue <= BigDecimal.ZERO) return false
    } catch (e: NumberFormatException) {
        return false
    }
    
    return true
}