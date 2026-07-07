package com.example.financial.presentation.screen.budgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.Budget
import com.example.financial.presentation.viewmodel.FinancialViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: FinancialViewModel,
    onAddExpenseBudgetClick: () -> Unit,
    onAddIncomeBudgetClick: () -> Unit,
    onAddBudgetsGroupClick: () -> Unit,
    onBudgetClick: (String) -> Unit,
    onEditBudgetClick: (String) -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddMenu by remember { mutableStateOf(false) }
    var sortByName by remember { mutableStateOf(true) }

    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
    var budgetForOptions by remember { mutableStateOf<Budget?>(null) }

    val filteredBudgets = remember(uiState.budgets, searchQuery, sortByName) {
        uiState.budgets.filter { budget ->
            budget.name.contains(searchQuery, ignoreCase = true)
        }.let { 
            if (sortByName) it.sortedBy { b -> b.name } 
            else it.sortedByDescending { b -> b.amount }
        }
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
                    text = "Budgets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    IconButton(onClick = { showAddMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Budget"
                        )
                    }

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Expense Budget") },
                            onClick = {
                                showAddMenu = false
                                onAddExpenseBudgetClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Income Budget") },
                            onClick = {
                                showAddMenu = false
                                onAddIncomeBudgetClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Add Budgets Group") },
                            onClick = {
                                showAddMenu = false
                                onAddBudgetsGroupClick()
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
                placeholder = { Text("Search budgets") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BudgetInfoItem(
                        title = "Remaining",
                        amount = uiState.remainingBudget,
                        modifier = Modifier.weight(1f)
                    )

                    BudgetInfoItem(
                        title = "Budgeted",
                        amount = uiState.totalBudgeted,
                        modifier = Modifier.weight(1f)
                    )
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
                    text = "MY BUDGETS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                TextButton(onClick = { sortByName = !sortByName }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (sortByName) "Sort by Name" else "Sort by Amount")
                }
            }
        }

        if (filteredBudgets.isEmpty() && uiState.budgetGroups.isEmpty()) {
            item {
                Text(
                    text = if (searchQuery.isBlank())
                        "No budgets added yet. Tap + to add one."
                    else
                        "No budgets found.",
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Grouped budgets
            uiState.budgetGroups.forEach { group ->
                item {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = group.color,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                val budgetsInGroup = filteredBudgets.filter { it.budgetGroupId == group.id }
                items(budgetsInGroup) { budget ->
                    BudgetItem(
                        budget = budget,
                        onClick = { onBudgetClick(budget.id) },
                        onLongClick = { budgetForOptions = it }
                    )
                }
            }
            
            // Ungrouped budgets
            val unGroupedBudgets = filteredBudgets.filter { it.budgetGroupId == null }
            if (unGroupedBudgets.isNotEmpty()) {
                if (uiState.budgetGroups.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ungrouped",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(unGroupedBudgets) { budget ->
                    BudgetItem(
                        budget = budget,
                        onClick = { onBudgetClick(budget.id) },
                        onLongClick = { budgetForOptions = it }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Options Modal
    if (budgetForOptions != null) {
        ModalBottomSheet(onDismissRequest = { budgetForOptions = null }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text(budgetForOptions!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    modifier = Modifier.clickable {
                        val id = budgetForOptions!!.id
                        budgetForOptions = null
                        onEditBudgetClick(id)
                    },
                    headlineContent = { Text("Edit Budget") },
                    leadingContent = { Icon(Icons.Default.Edit, null) }
                )

                ListItem(
                    modifier = Modifier.clickable {
                        budgetToDelete = budgetForOptions
                        budgetForOptions = null
                    },
                    headlineContent = { Text("Delete Budget", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }

    // Delete Confirmation
    if (budgetToDelete != null) {
        AlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete '${budgetToDelete!!.name}'? This will not delete the associated transactions.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBudget(budgetToDelete!!)
                        budgetToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { budgetToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetItem(budget: Budget, onClick: () -> Unit, onLongClick: (Budget) -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(budget)
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (budget.isIncome) "Income" else "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (budget.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { budget.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = budget.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f of $%.2f", budget.spent, budget.amount),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f left", budget.remaining),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (budget.remaining < 0) Color.Red else Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun BudgetInfoItem(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
