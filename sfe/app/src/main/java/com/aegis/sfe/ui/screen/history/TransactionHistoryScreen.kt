package com.aegis.sfe.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.sfe.data.model.TransactionResponse
import com.aegis.sfe.data.model.TransactionStatus
import com.aegis.sfe.data.model.TransactionType
import com.aegis.sfe.ui.viewmodel.BankingViewModel
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: BankingViewModel = viewModel()
) {
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val transactionHistory by viewModel.transactionHistory.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(selectedAccount) {
        selectedAccount?.let { account ->
            viewModel.loadTransactionHistory(account.accountNumber)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            selectedAccount?.let { account ->
                                viewModel.loadTransactionHistory(account.accountNumber)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            // Account Info Header
            selectedAccount?.let { account ->
                AccountInfoHeader(account = account)
                HorizontalDivider()
            }
            
            // Transaction List
            when {
                uiState.isLoading && transactionHistory == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                transactionHistory?.content?.isEmpty() == true -> {
                    EmptyTransactionList()
                }
                
                transactionHistory != null -> {
                    TransactionList(
                        transactions = transactionHistory!!.content,
                        onLoadMore = {
                            // TODO: Implement pagination
                        }
                    )
                }
            }
            
            // Error handling
            uiState.error?.let { error ->
                ErrorCard(
                    error = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun AccountInfoHeader(account: com.aegis.sfe.data.model.Account) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = account.accountType.name.replace("_", " "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = account.accountNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current Balance: ${currencyFormat.format(account.balance)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TransactionList(
    transactions: List<TransactionResponse>,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction)
        }
        
        // TODO: Add load more functionality
        item {
            if (transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "End of transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: TransactionResponse) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getTransactionTitle(transaction),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = transaction.transactionReference,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = getAmountDisplay(transaction, currencyFormat),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = getAmountColor(transaction)
                    )
                    TransactionStatusChip(status = TransactionStatus.valueOf(transaction.status))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            transaction.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = try {
                        LocalDateTime.parse(transaction.createdAt).format(dateFormatter)
                    } catch (e: Exception) {
                        transaction.createdAt
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secured",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Secured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionStatusChip(status: TransactionStatus) {
    val (backgroundColor, contentColor) = when (status) {
        TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        TransactionStatus.PROCESSING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        TransactionStatus.REFUNDED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyTransactionList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "No transactions",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No transactions found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Your transaction history will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                    text = "Error Loading History",
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
private fun getAmountColor(transaction: TransactionResponse): androidx.compose.ui.graphics.Color {
    return when (TransactionType.valueOf(transaction.transactionType)) {
        TransactionType.TRANSFER -> {
            // For transfers, we need to determine if it's incoming or outgoing
            // This is a simplified approach - in a real app, you'd have more context
            MaterialTheme.colorScheme.primary
        }
        TransactionType.DEPOSIT -> MaterialTheme.colorScheme.primary
        TransactionType.WITHDRAWAL -> MaterialTheme.colorScheme.error
        TransactionType.PAYMENT -> MaterialTheme.colorScheme.error
        TransactionType.REFUND -> MaterialTheme.colorScheme.primary
    }
}

private fun getAmountDisplay(transaction: TransactionResponse, currencyFormat: NumberFormat): String {
    val amount = transaction.amount
    val type = TransactionType.valueOf(transaction.transactionType)
    
    return when (type) {
        TransactionType.WITHDRAWAL, TransactionType.PAYMENT -> "-${currencyFormat.format(amount)}"
        TransactionType.DEPOSIT, TransactionType.REFUND -> "+${currencyFormat.format(amount)}"
        TransactionType.TRANSFER -> currencyFormat.format(amount) // Context-dependent
    }
}

private fun getTransactionTitle(transaction: TransactionResponse): String {
    val type = TransactionType.valueOf(transaction.transactionType)
    
    return when (type) {
        TransactionType.TRANSFER -> "Money Transfer"
        TransactionType.DEPOSIT -> "Deposit"
        TransactionType.WITHDRAWAL -> "Withdrawal"
        TransactionType.PAYMENT -> "Payment"
        TransactionType.REFUND -> "Refund"
    }
}