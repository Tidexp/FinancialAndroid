package com.example.financialtest.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financialtest.presentation.component.AccountItem
import com.example.financialtest.presentation.component.NetWorthCard
import com.example.financialtest.presentation.component.SectionHeader
import com.example.financialtest.presentation.viewmodel.FinancialViewModel

@Composable
fun AccountsScreen(
    viewModel: FinancialViewModel,
    onAddAccountClick: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccountClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                uiState.balanceData?.let { balance ->
                    NetWorthCard(balance)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader("MY ACCOUNTS")
            }

            if (uiState.accounts.isEmpty()) {
                item {
                    Text(
                        "No accounts added yet. Tap + to add one manually.",
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(uiState.accounts) { account ->
                    AccountItem(account)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
