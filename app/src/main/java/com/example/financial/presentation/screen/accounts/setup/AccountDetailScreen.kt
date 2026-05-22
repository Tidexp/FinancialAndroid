package com.example.financial.presentation.screen.accounts.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    account: Account,
    groupName: String?,
    onBackClick: () -> Unit,
    onDeleteClick: (Account) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Account Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = account.color.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(account.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(account.type.displayName, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(account.balance, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = account.color)
                }
            }

            // Info Section
            Text("Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column {
                    DetailItem(Icons.Default.Layers, "Group", groupName ?: "None")
                    DetailDivider()
                    
                    // Show specific fields based on type
                    when (account.type) {
                        AccountType.CREDIT -> {
                            DetailItem(Icons.Default.CreditCard, "Credit Limit", account.creditLimit ?: "N/A")
                            DetailDivider()
                            DetailItem(Icons.Default.SyncAlt, "Statement Close Day", account.statementCloseDay ?: "N/A")
                        }
                        AccountType.LOAN -> {
                            DetailItem(Icons.Default.Money, "Principal", account.principalAmount ?: "N/A")
                            DetailDivider()
                            DetailItem(Icons.Default.Percent, "APR", account.apr ?: "N/A")
                        }
                        AccountType.INVESTMENT -> {
                            DetailItem(Icons.Default.CalendarToday, "As of Date", account.asOfDate ?: "N/A")
                        }
                        AccountType.FOREX -> {
                            DetailItem(Icons.Default.CurrencyExchange, "Currency", account.currency ?: "N/A")
                        }
                        else -> {}
                    }
                    
                    if (account.additionalInfo?.isNotBlank() == true) {
                        DetailDivider()
                        DetailItem(Icons.AutoMirrored.Filled.Notes, "Notes", account.additionalInfo)
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to delete '${account.name}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteClick(account)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        headlineContent = { Text(label, color = Color.Gray) },
        trailingContent = { Text(value, fontWeight = FontWeight.Medium) }
    )
}

@Composable
fun DetailDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}
