package com.example.financial.presentation.screen.scheduled

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.presentation.component.TransactionItem
import com.example.financial.presentation.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(
    viewModel: FinancialViewModel,
    onAddClick: () -> Unit = {},
    onEditScheduled: (Transaction) -> Unit = {}
) {
    val uiState by viewModel.homeUiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRange by remember { mutableStateOf("This Month") }
    var showRangeMenu by remember { mutableStateOf(false) }

    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSkipConfirm by remember { mutableStateOf(false) }

    val scheduledTransactions = remember(uiState.transactions, searchQuery) {
        uiState.transactions.filter { 
            it.status == TransactionStatus.PLANNED &&
            (it.payee?.contains(searchQuery, ignoreCase = true) == true ||
             it.description?.contains(searchQuery, ignoreCase = true) == true)
        }.sortedBy { it.date }
    }

    val groupedTransactions = remember(scheduledTransactions) {
        scheduledTransactions.groupBy {
            SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date(it.date))
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val ranges = listOf(
        "This Week",
        "This Month",
        "This Year",
        "Next Week",
        "Next Month",
        "Next 3 Months",
        "Next Year",
        "Over Due"
    )

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
                    text = "Scheduled",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Scheduled"
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search scheduled") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp)
            )
        }

        item {
            // Calendar View
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        headline = null
                    )
                }
            }
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalExpenses = scheduledTransactions.filter { it.amount < 0 }.sumOf { it.amount }
                    val totalIncome = scheduledTransactions.filter { it.amount > 0 }.sumOf { it.amount }

                    ScheduledInfoItem(
                        title = "Expenses",
                        amount = String.format(Locale.getDefault(), "$%.2f", Math.abs(totalExpenses)),
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRangeMenu = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Range",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = selectedRange,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRangeMenu,
                            onDismissRequest = { showRangeMenu = false }
                        ) {
                            ranges.forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range) },
                                    onClick = {
                                        selectedRange = range
                                        showRangeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    ScheduledInfoItem(
                        title = "Income",
                        amount = String.format(Locale.getDefault(), "$%.2f", totalIncome),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        groupedTransactions.forEach { (date, transactions) ->
            item {
                Text(
                    text = date.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(transactions) { transaction ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TransactionItem(
                            transaction = transaction,
                            onClick = {
                                selectedTransaction = transaction
                                showOptionsMenu = true
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showOptionsMenu && selectedTransaction != null) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsMenu = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                ListItem(
                    headlineContent = { Text("Pay") },
                    leadingContent = { Icon(Icons.Default.CheckCircle, null) },
                    modifier = Modifier.clickable {
                        viewModel.payScheduledTransaction(selectedTransaction!!)
                        showOptionsMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Skip") },
                    leadingContent = { Icon(Icons.Default.SkipNext, null) },
                    modifier = Modifier.clickable {
                        showSkipConfirm = true
                        showOptionsMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Edit") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.clickable {
                        onEditScheduled(selectedTransaction!!)
                        showOptionsMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Duplicate") },
                    leadingContent = { Icon(Icons.Default.ContentCopy, null) },
                    modifier = Modifier.clickable {
                        viewModel.addTransaction(selectedTransaction!!.copy(id = UUID.randomUUID().toString()))
                        showOptionsMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Previous payment") },
                    leadingContent = { Icon(Icons.Default.Refresh, null) },
                    modifier = Modifier.clickable { /* TODO */ }
                )
                ListItem(
                    headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        showDeleteConfirm = true
                        showOptionsMenu = false
                    }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Scheduled Transaction") },
            text = { Text("Are you sure you want to delete this scheduled transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(selectedTransaction!!)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showSkipConfirm && selectedTransaction != null) {
        val repeatInfo = selectedTransaction!!.recurrence?.let {
            if (it.frequencyValue == 1) "Every ${it.frequencyUnit}" else "Every ${it.frequencyValue} ${it.frequencyUnit}s"
        } ?: "One-time"

        AlertDialog(
            onDismissRequest = { showSkipConfirm = false },
            title = { Text("Skip Scheduled Transaction") },
            text = { Text("Are you sure you want to skip this occurrence ($repeatInfo)?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.skipScheduledTransaction(selectedTransaction!!)
                        showSkipConfirm = false
                    }
                ) { Text("Skip") }
            },
            dismissButton = {
                TextButton(onClick = { showSkipConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ScheduledInfoItem(
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

