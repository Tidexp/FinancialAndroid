package com.example.financial.presentation.screen.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financial.presentation.component.AccountItem
import com.example.financial.presentation.component.AccountGroupItem
import com.example.financial.presentation.viewmodel.FinancialViewModel

@Composable
fun AccountsScreen(
    viewModel: FinancialViewModel,
    onAddAccountClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onAccountClick: (String) -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddMenu by remember { mutableStateOf(false) }

    val filteredAccounts = uiState.accounts.filter { account ->
        account.name.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    IconButton(onClick = { showAddMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Account") },
                            onClick = {
                                showAddMenu = false
                                onAddAccountClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Add Group Account") },
                            onClick = {
                                showAddMenu = false
                                onAddGroupClick()
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search accounts") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp)
            )
        }

        item {
            uiState.balanceData?.let { balance ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BalanceInfoItem(
                                title = "Net Worth",
                                amount = balance.netWorth,
                                modifier = Modifier.weight(1f)
                            )

                            BalanceInfoItem(
                                title = "Liabilities",
                                amount = balance.liabilities,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Auth: ${uiState.authStatus}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.authStatus.contains("Connected")) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "DB: ${uiState.dbStatus}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* TODO: open all transactions */ }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "All Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY ACCOUNTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                TextButton(onClick = { /* TODO: sort accounts */ }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }
            }
        }

        if (filteredAccounts.isEmpty() && uiState.accountGroups.isEmpty()) {
            item {
                Text(
                    text = if (searchQuery.isBlank())
                        "No accounts added yet. Tap + to add one manually."
                    else
                        "No accounts found.",
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Logic: Duyệt qua các group, hiển thị group và các account thuộc group đó
            uiState.accountGroups.forEach { group ->
                item(key = group.id) {
                    AccountGroupItem(group)
                }
                
                val accountsInGroup = filteredAccounts.filter { it.groupId == group.id }
                items(accountsInGroup, key = { it.id }) { account ->
                    Box(modifier = Modifier.padding(start = 16.dp)) {
                        AccountItem(account, onClick = { onAccountClick(it.id) })
                    }
                }
            }
            
            // Hiển thị các Account không thuộc group nào (Ungrouped)
            val unGroupedAccounts = filteredAccounts.filter { it.groupId == null }
            if (unGroupedAccounts.isNotEmpty()) {
                if (uiState.accountGroups.isNotEmpty()) {
                    item {
                        Text(
                            "Ungrouped",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(unGroupedAccounts, key = { it.id }) { account ->
                    AccountItem(account, onClick = { onAccountClick(it.id) })
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BalanceInfoItem(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
