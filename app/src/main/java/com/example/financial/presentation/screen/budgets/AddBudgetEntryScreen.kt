package com.example.financial.presentation.screen.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Budget
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.screen.budgets.setup.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetEntryScreen(
    budget: Budget,
    onCloseClick: () -> Unit,
    onSaveClick: (Transaction) -> Unit
) {
    var entryName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (budget.isIncome) "New Income Entry" else "New Expense Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val amountValue = amountText.toDoubleOrNull() ?: 0.0
                            if (entryName.isNotBlank() && amountValue > 0) {
                                onSaveClick(
                                    Transaction(
                                        id = UUID.randomUUID().toString(),
                                        type = if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                        fromAccountId = "budget_simulated", // Không liên kết account thật
                                        amount = amountValue,
                                        payee = entryName,
                                        date = selectedDate,
                                        budgetId = budget.id, // Ràng buộc vào budget này
                                        status = TransactionStatus.CLEARED
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6))
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            BudgetSectionHeader("Describe your entry")
            BudgetCard {
                BudgetInputRow(Icons.Default.Badge, "Name", entryName) { entryName = it }
                BudgetDivider()
                
                ListItem(
                    leadingContent = { Icon(Icons.Default.MoveToInbox, null, tint = Color.Gray) },
                    headlineContent = {
                        TextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    trailingContent = { Text("USD", color = Color.Gray) }
                )
            }

            BudgetSectionHeader("Which transactions should be included?")
            BudgetCard {
                ListItem(
                    leadingContent = { Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.Gray) },
                    headlineContent = { Text("Budget-only entry") },
                    supportingContent = { Text("This will not affect your real accounts.", color = Color.Gray) }
                )
                BudgetDivider()
                
                ListItem(
                    leadingContent = { Icon(Icons.Outlined.Folder, null, tint = Color.Gray) },
                    headlineContent = { Text(budget.categories.firstOrNull() ?: "General") }
                )
            }

            BudgetSectionHeader("Configure date")
            BudgetCard {
                ListItem(
                    modifier = Modifier.clickable { showDatePicker = true },
                    leadingContent = { Icon(Icons.Default.CalendarToday, null, tint = Color.Gray) },
                    headlineContent = {
                        Surface(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                dateFormatter.format(Date(selectedDate)),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    },
                    trailingContent = { Text("Entry date", color = Color.Gray, fontSize = 14.sp) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
