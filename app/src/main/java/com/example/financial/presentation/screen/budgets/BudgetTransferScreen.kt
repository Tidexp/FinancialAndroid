package com.example.financial.presentation.screen.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Budget
import com.example.financial.presentation.component.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTransferScreen(
    showHeader: Boolean = true,
    initialFromBudget: Budget? = null,
    allBudgets: List<Budget>,
    onSave: (fromId: String, toId: String, amount: Double, memo: String, date: Long) -> Unit = { _, _, _, _, _ -> },
    onCloseClick: () -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var fromBudget by remember { mutableStateOf(initialFromBudget) }
    var toBudget by remember { mutableStateOf<Budget?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    // Picker states
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    var transactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transactionDate)
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = transactionDate }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = transactionDate }.get(Calendar.MINUTE)
    )
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val saveAction = {
        val amountValue = parseNumericInput(amount)
        if (amountValue > 0 && fromBudget != null && toBudget != null) {
            onSave(fromBudget!!.id, toBudget!!.id, amountValue, memo, transactionDate)
        }
    }

    LaunchedEffect(fromBudget, toBudget, amount, description, transactionDate) {
        onRegisterSaveAction(saveAction)
        if (fromBudget != null && toBudget != null && description.isEmpty()) {
            description = "Transfer ${fromBudget?.name} -> ${toBudget?.name}"
        }
    }

    TransactionBaseScreen(
        title = "Budget Transfer",
        onCloseClick = onCloseClick,
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {}
    ) {
        // From Budget
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFromPicker = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Email, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = fromBudget?.name ?: "Select source budget",
                color = if (fromBudget != null) Color.Black else Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
        
        // Swap Button
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterEnd) {
             IconButton(onClick = {
                 val temp = fromBudget
                 fromBudget = toBudget
                 toBudget = temp
             }) {
                 Icon(Icons.Default.SwapVert, null, tint = Color.Gray)
             }
        }

        // To Budget
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showToPicker = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Mail, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = toBudget?.name ?: "Select target budget",
                color = if (toBudget != null) Color.Black else Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
        TransactionDivider()

        // Amount Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AddCircle, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (amount.isEmpty()) Text("0,00", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
            Text("Transfer amount", color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Text("USD", color = Color.Gray, fontSize = 16.sp)
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        TransactionDivider()

        // Description
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
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        TransactionDivider()

        // Date & Time
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Surface(onClick = { showDatePicker = true }, color = Color.Transparent) {
                TransactionDateBadge(dateFormatter.format(Date(transactionDate)))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(onClick = { showTimePicker = true }, color = Color.Transparent) {
                TransactionDateBadge(timeFormatter.format(Date(transactionDate)))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Date & time", color = Color.Gray, fontSize = 14.sp)
        }
        TransactionDivider()

        // Memo
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
        TransactionDivider()
        
        // Tags and Image placeholder
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Icon(Icons.Outlined.Image, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
             Spacer(modifier = Modifier.weight(1f))
             IconButton(onClick = {}, modifier = Modifier.size(24.dp).background(Color(0xFF3478F6).copy(alpha = 0.1f), RoundedCornerShape(4.dp))) {
                 Icon(Icons.Default.Add, null, tint = Color(0xFF3478F6), modifier = Modifier.size(16.dp))
             }
        }
        TransactionDivider()
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Sell, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Tags", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }

    if (showFromPicker) {
        ModalBottomSheet(onDismissRequest = { showFromPicker = false }) {
            PickerSheetContent("Select Budget", allBudgets, { it.name }, { Icons.Default.Circle }, { it.color }, {
                fromBudget = it
                showFromPicker = false
            })
        }
    }

    if (showToPicker) {
        ModalBottomSheet(onDismissRequest = { showToPicker = false }) {
            PickerSheetContent("Select Budget", allBudgets, { it.name }, { Icons.Default.Circle }, { it.color }, {
                toBudget = it
                showToPicker = false
            })
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { transactionDate = it }
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
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = transactionDate
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    transactionDate = cal.timeInMillis
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
}
