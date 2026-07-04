package com.example.financial.presentation.screen.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.AccountGroup
import com.example.financial.domain.model.AccountType
import com.example.financial.presentation.component.AccountItem
import com.example.financial.presentation.component.AccountGroupItem
import com.example.financial.presentation.viewmodel.FinancialViewModel

enum class AccountSortOrder {
    NAME, BALANCE, MANUAL
}

@Composable
fun AccountsScreen(
    viewModel: FinancialViewModel,
    onAddAccountClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onAccountClick: (String) -> Unit,
    onNavigateToTransaction: (String, String) -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddMenu by remember { mutableStateOf(false) }

    var selectedAccountForMenu by remember { mutableStateOf<Account?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Account?>(null) }

    var selectedGroupForMenu by remember { mutableStateOf<AccountGroup?>(null) }
    var showDeleteGroupConfirmDialog by remember { mutableStateOf<AccountGroup?>(null) }

    val expandedGroupIds = remember { mutableStateListOf<String>() }
    var showReorderMenu by remember { mutableStateOf(false) }
    var currentSortOrder by remember { mutableStateOf(AccountSortOrder.MANUAL) }
    var isReorderMode by remember { mutableStateOf(false) }

    // Helper to parse balance string to Double for sorting
    fun parseBalance(balance: String): Double {
        return try {
            balance.replace(Regex("[^0-9.-]"), "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    val filteredAccounts = remember(uiState.accounts, searchQuery, currentSortOrder) {
        val filtered = uiState.accounts.filter { account ->
            account.name.contains(searchQuery, ignoreCase = true)
        }
        when (currentSortOrder) {
            AccountSortOrder.NAME -> filtered.sortedBy { it.name }
            AccountSortOrder.BALANCE -> filtered.sortedByDescending { parseBalance(it.balance) }
            AccountSortOrder.MANUAL -> filtered.sortedBy { it.orderIndex }
        }
    }

    val sortedGroups = remember(uiState.accountGroups, currentSortOrder) {
        when (currentSortOrder) {
            AccountSortOrder.NAME -> uiState.accountGroups.sortedBy { it.name }
            AccountSortOrder.MANUAL -> uiState.accountGroups.sortedBy { it.orderIndex }
            else -> uiState.accountGroups
        }
    }

    // Initialize expanded groups
    LaunchedEffect(uiState.accountGroups) {
        uiState.accountGroups.forEach { group ->
            if (!expandedGroupIds.contains(group.id)) {
                expandedGroupIds.add(group.id)
            }
        }
    }

    // Helper for transaction options
    fun getTransactionOptions(type: AccountType): List<String> {
        val options = mutableListOf("Expense", "Income", "Transfer", "Adjust Balance")
        if (type == AccountType.FOREX) options.add("Exchange")
        if (type == AccountType.INVESTMENT) {
            options.add("Buy")
            options.add("Sell")
        }
        return options
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            Icon(Icons.Default.Add, contentDescription = "Add")
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
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
                                Icon(Icons.Default.ReceiptLong, contentDescription = null)
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
                    Box {
                        if (isReorderMode) {
                            TextButton(onClick = { 
                                isReorderMode = false
                            }) {
                                Text("Done", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            TextButton(onClick = { showReorderMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Reorder,
                                    contentDescription = "Reorder",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reorder")
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showReorderMenu,
                            onDismissRequest = { showReorderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Manual Reorder") },
                                onClick = { 
                                    isReorderMode = true
                                    currentSortOrder = AccountSortOrder.MANUAL
                                    showReorderMenu = false 
                                },
                                leadingIcon = { Icon(Icons.Default.DragHandle, contentDescription = null) },
                                trailingIcon = { if(currentSortOrder == AccountSortOrder.MANUAL) Icon(Icons.Default.Check, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Sort by Name") },
                                onClick = { 
                                    currentSortOrder = AccountSortOrder.NAME
                                    showReorderMenu = false 
                                },
                                leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
                                trailingIcon = { if(currentSortOrder == AccountSortOrder.NAME) Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Balance") },
                                onClick = { 
                                    currentSortOrder = AccountSortOrder.BALANCE
                                    showReorderMenu = false 
                                },
                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                trailingIcon = { if(currentSortOrder == AccountSortOrder.BALANCE) Icon(Icons.Default.Check, null) }
                            )
                        }
                    }
                }
            }

            if (filteredAccounts.isEmpty() && uiState.accountGroups.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isBlank()) "No accounts added yet. Tap + to add one manually." else "No accounts found.",
                        modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                sortedGroups.forEachIndexed { index, group ->
                    val isExpanded = expandedGroupIds.contains(group.id)
                    item(key = group.id) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isReorderMode) {
                                Column {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val list = sortedGroups.toMutableList()
                                                val item = list.removeAt(index)
                                                list.add(index - 1, item)
                                                viewModel.updateGroupOrder(list)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, null)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (index < sortedGroups.size - 1) {
                                                val list = sortedGroups.toMutableList()
                                                val item = list.removeAt(index)
                                                list.add(index + 1, item)
                                                viewModel.updateGroupOrder(list)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, null)
                                    }
                                }
                            }
                            AccountGroupItem(
                                group = group,
                                modifier = Modifier.weight(1f),
                                isExpanded = isExpanded,
                                onClick = {
                                    if (isExpanded) expandedGroupIds.remove(group.id)
                                    else expandedGroupIds.add(group.id)
                                },
                                onLongClick = { selectedGroupForMenu = it }
                            )
                        }
                    }
                    
                    if (isExpanded) {
                        val accountsInGroup = filteredAccounts.filter { it.groupId == group.id }
                        itemsIndexed(accountsInGroup, key = { _, it -> it.id }) { accIndex, account ->
                            Row(
                                modifier = Modifier.padding(start = if (isReorderMode) 0.dp else 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isReorderMode) {
                                    Column {
                                        IconButton(
                                            onClick = {
                                                if (accIndex > 0) {
                                                    val list = accountsInGroup.toMutableList()
                                                    val item = list.removeAt(accIndex)
                                                    list.add(accIndex - 1, item)
                                                    viewModel.updateAccountOrder(list)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowUp, null)
                                        }
                                        IconButton(
                                            onClick = {
                                                if (accIndex < accountsInGroup.size - 1) {
                                                    val list = accountsInGroup.toMutableList()
                                                    val item = list.removeAt(accIndex)
                                                    list.add(accIndex + 1, item)
                                                    viewModel.updateAccountOrder(list)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, null)
                                        }
                                    }
                                }
                                AccountItem(
                                    account = account,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onAccountClick(account.id) },
                                    onLongClick = { selectedAccountForMenu = it }
                                )
                            }
                        }
                    }
                }
                
                val unGroupedAccounts = filteredAccounts.filter { it.groupId == null }
                if (unGroupedAccounts.isNotEmpty()) {
                    if (uiState.accountGroups.isNotEmpty()) {
                        item {
                            Text("Ungrouped", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                    itemsIndexed(unGroupedAccounts, key = { _, it -> it.id }) { accIndex, account ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isReorderMode) {
                                Column {
                                    IconButton(
                                        onClick = {
                                            if (accIndex > 0) {
                                                val list = unGroupedAccounts.toMutableList()
                                                val item = list.removeAt(accIndex)
                                                list.add(accIndex - 1, item)
                                                viewModel.updateAccountOrder(list)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, null)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (accIndex < unGroupedAccounts.size - 1) {
                                                val list = unGroupedAccounts.toMutableList()
                                                val item = list.removeAt(accIndex)
                                                list.add(accIndex + 1, item)
                                                viewModel.updateAccountOrder(list)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, null)
                                    }
                                }
                            }
                            AccountItem(
                                account = account,
                                modifier = Modifier.weight(1f),
                                onClick = { onAccountClick(account.id) },
                                onLongClick = { selectedAccountForMenu = it }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Account Context Menu
        selectedAccountForMenu?.let { account ->
            DropdownMenu(
                expanded = true,
                onDismissRequest = { selectedAccountForMenu = null }
            ) {
                Text(
                    text = account.name,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                
                getTransactionOptions(account.type).forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onNavigateToTransaction(account.id, option)
                            selectedAccountForMenu = null
                        },
                        leadingIcon = {
                            val icon = when(option) {
                                "Expense" -> Icons.Outlined.RemoveCircleOutline
                                "Income" -> Icons.Outlined.AddCircleOutline
                                "Transfer" -> Icons.Default.SwapHoriz
                                "Exchange" -> Icons.Outlined.Cached
                                "Buy" -> Icons.Outlined.ArrowCircleUp
                                "Sell" -> Icons.Outlined.ArrowCircleDown
                                else -> Icons.Default.Adjust
                            }
                            Icon(icon, contentDescription = null)
                        }
                    )
                }
                
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Edit Account") },
                    onClick = {
                        // TODO: onEditAccountClick(account)
                        selectedAccountForMenu = null
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Account") },
                    onClick = {
                        showDeleteConfirmDialog = account
                        selectedAccountForMenu = null
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
                )
            }
        }

        // Account Deletion Confirmation Dialog
        showDeleteConfirmDialog?.let { account ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to delete '${account.name}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAccount(account)
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Group Context Menu
        selectedGroupForMenu?.let { group ->
            DropdownMenu(
                expanded = true,
                onDismissRequest = { selectedGroupForMenu = null }
            ) {
                Text(
                    text = group.name,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Edit Group") },
                    onClick = {
                        // TODO: onEditGroupClick(group)
                        selectedGroupForMenu = null
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Group") },
                    onClick = {
                        showDeleteGroupConfirmDialog = group
                        selectedGroupForMenu = null
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
                )
            }
        }

        // Group Deletion Confirmation Dialog
        showDeleteGroupConfirmDialog?.let { group ->
            AlertDialog(
                onDismissRequest = { showDeleteGroupConfirmDialog = null },
                title = { Text("Delete Group") },
                text = { Text("Are you sure you want to delete '${group.name}'? This will not delete the accounts inside it.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAccountGroup(group)
                            showDeleteGroupConfirmDialog = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteGroupConfirmDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun BalanceInfoItem(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
