package com.example.financial.presentation.screen.scheduled.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import com.example.financial.presentation.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledExpenseScreen(
    viewModel: FinancialViewModel,
    showHeader: Boolean = true,
    account: Account? = null,
    existingTransaction: Transaction? = null,
    onSave: (Transaction) -> Unit = {},
    onCloseClick: () -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    val uiState by viewModel.homeUiState.collectAsState()
    
    var amount by remember { mutableStateOf(existingTransaction?.amount?.toString() ?: "") }
    var payee by remember { mutableStateOf(existingTransaction?.payee ?: "") }
    var category by remember { mutableStateOf(existingTransaction?.categoryId ?: "") }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }
    var memo by remember { mutableStateOf(existingTransaction?.memo ?: "") }
    var isAutoPay by remember { mutableStateOf(true) }

    var selectedAccount by remember { mutableStateOf(account ?: uiState.accounts.find { it.id == existingTransaction?.fromAccountId }) }
    var showAccountPicker by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableLongStateOf(existingTransaction?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    val calendar = remember(existingTransaction) {
        Calendar.getInstance().apply {
            if (existingTransaction != null) timeInMillis = existingTransaction.date
        }
    }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Recurrence states
    var repeatType by remember { mutableStateOf(existingTransaction?.recurrence?.frequencyUnit ?: "Month") }
    var showRepeatPicker by remember { mutableStateOf(false) }
    
    // Custom recurrence states
    var frequencyValue by remember { mutableStateOf(existingTransaction?.recurrence?.frequencyValue?.toString() ?: "1") }
    var frequencyUnit by remember { mutableStateOf("Month") }
    var daySpecOrdinal by remember { mutableStateOf("First") }
    var daySpecWeekday by remember { mutableStateOf("Monday") }
    var showCustomRecurrenceDialog by remember { mutableStateOf(false) }

    // End setting states
    var endType by remember { mutableStateOf(existingTransaction?.recurrence?.endType ?: "Never") }
    var endAfterCount by remember { mutableStateOf(existingTransaction?.recurrence?.endAfterCount?.toString() ?: "1") }
    var showEndPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val saveAction = {
        val amountValue = parseNumericInput(amount)
        if (amountValue > 0 && selectedAccount != null) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedDate
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            onSave(
                Transaction(
                    id = existingTransaction?.id ?: UUID.randomUUID().toString(),
                    type = TransactionType.EXPENSE,
                    fromAccountId = selectedAccount!!.id,
                    amount = amountValue,
                    payee = payee,
                    categoryId = category, // Using it as name for now
                    description = description,
                    memo = memo,
                    date = calendar.timeInMillis,
                    status = TransactionStatus.PLANNED,
                    recurrence = com.example.financial.domain.model.Recurrence(
                        frequencyValue = frequencyValue.toIntOrNull() ?: 1,
                        frequencyUnit = if (repeatType == "Custom") frequencyUnit else repeatType,
                        endType = endType,
                        endAfterCount = endAfterCount.toIntOrNull() ?: 1
                    )
                )
            )
        }
    }

    LaunchedEffect(amount, payee, description, memo, selectedAccount, selectedDate, selectedHour, selectedMinute) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Scheduled Expense",
        onCloseClick = onCloseClick,
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {}
    ) {
        ExpenseItem(
            icon = Icons.Outlined.CreditCard,
            label = "Select Account",
            value = selectedAccount?.name ?: "Select",
            onClick = { showAccountPicker = true }
        )
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AddCircle, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                decorationBox = { inner ->
                    if (amount.isEmpty()) Text("0,00", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
            Text("USD", color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Person, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = payee,
                onValueChange = { payee = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (payee.isEmpty()) Text("Payee", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.FontDownload, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (description.isEmpty()) Text("Description", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Folder, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (category.isEmpty()) Text("Type in Category", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
        }
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                TransactionDateBadge(dateFormatter.format(Date(selectedDate)))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.clickable { showTimePicker = true }) {
                TransactionDateBadge(String.format("%02d:%02d", selectedHour, selectedMinute))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Date & time", color = Color.Gray, fontSize = 14.sp)
        }
        TransactionDivider()

        val repeatDisplayValue = if (repeatType == "Custom") {
            "Every $frequencyValue ${frequencyUnit.lowercase()}"
        } else {
            "Every $repeatType"
        }

        val endDisplayValue = if (endType == "After") "After $endAfterCount repeats" else "Never"

        ScheduledTransactionFields(
            isAutoPay = isAutoPay,
            onAutoPayChange = { isAutoPay = it },
            repeatValue = repeatDisplayValue,
            onRepeatClick = { showRepeatPicker = true },
            endValue = endDisplayValue,
            onEndClick = { showEndPicker = true }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Description, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = memo,
                onValueChange = { memo = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (memo.isEmpty()) Text("Memo", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
        }
    }

    // --- Dialogs & Sheets ---

    if (showAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Account", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(uiState.accounts) { acc ->
                        ListItem(
                            modifier = Modifier.clickable {
                                selectedAccount = acc
                                showAccountPicker = false
                            },
                            headlineContent = { Text(acc.name) },
                            leadingContent = { Box(modifier = Modifier.size(24.dp).background(acc.color, CircleShape)) }
                        )
                    }
                }
            }
        }
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

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showRepeatPicker) {
        val options = listOf("Day", "Week", "Month", "Year", "Custom")
        ModalBottomSheet(onDismissRequest = { showRepeatPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Repeat", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { option ->
                    ListItem(
                        modifier = Modifier.clickable {
                            if (option == "Custom") {
                                showCustomRecurrenceDialog = true
                            } else {
                                repeatType = option
                                frequencyValue = "1"
                                frequencyUnit = option
                            }
                            showRepeatPicker = false
                        },
                        headlineContent = { Text("Every $option") },
                        trailingContent = {
                            if (repeatType == option) Icon(Icons.Default.Check, null, tint = Color(0xFF3478F6))
                        }
                    )
                }
            }
        }
    }

    if (showCustomRecurrenceDialog) {
        AlertDialog(
            onDismissRequest = { showCustomRecurrenceDialog = false },
            title = { Text("Custom Recurrence") },
            text = {
                Column {
                    Text("Frequency", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = frequencyValue,
                            onValueChange = { frequencyValue = it },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(frequencyUnit)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("Day", "Week", "Month", "Year").forEach {
                                    DropdownMenuItem(text = { Text(it) }, onClick = { frequencyUnit = it; expanded = false })
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Day of week", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var exp1 by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { exp1 = true }) {
                                Text(daySpecOrdinal)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = exp1, onDismissRequest = { exp1 = false }) {
                                listOf("First", "2nd", "3rd", "4th", "Last").forEach {
                                    DropdownMenuItem(text = { Text(it) }, onClick = { daySpecOrdinal = it; exp1 = false })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        var exp2 by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { exp2 = true }) {
                                Text(daySpecWeekday)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = exp2, onDismissRequest = { exp2 = false }) {
                                listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEach {
                                    DropdownMenuItem(text = { Text(it) }, onClick = { daySpecWeekday = it; exp2 = false })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    repeatType = "Custom"
                    showCustomRecurrenceDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRecurrenceDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEndPicker) {
        ModalBottomSheet(onDismissRequest = { showEndPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("End Recurrence", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                ListItem(
                    modifier = Modifier.clickable {
                        endType = "Never"
                        showEndPicker = false
                    },
                    headlineContent = { Text("Never") },
                    trailingContent = { if (endType == "Never") Icon(Icons.Default.Check, null, tint = Color(0xFF3478F6)) }
                )
                ListItem(
                    modifier = Modifier.clickable {
                        endType = "After"
                    },
                    headlineContent = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("After ")
                            BasicTextField(
                                value = endAfterCount,
                                onValueChange = { endAfterCount = it },
                                modifier = Modifier.width(40.dp).background(Color(0xFFF2F2F7), RoundedCornerShape(4.dp)).padding(4.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text(" repeats")
                        }
                    },
                    trailingContent = {
                        if (endType == "After") {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF3478F6))
                        }
                    }
                )
            }
        }
    }
}
