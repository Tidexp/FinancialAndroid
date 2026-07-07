package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    showHeader: Boolean = true,
    account: Account? = null,
    allAccounts: List<Account> = emptyList(),
    initialPayee: String = "",
    initialCategory: String = "",
    onSave: (Transaction) -> Unit = {},
    onCloseClick: () -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var selectedAccount by remember { mutableStateOf(account) }
    var amount by remember { mutableStateOf("") }
    var payee by remember { mutableStateOf(initialPayee) }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(initialCategory) }
    var memo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TransactionStatus.CLEARED) }
    var transactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Picker states
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transactionDate)
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = transactionDate }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = transactionDate }.get(Calendar.MINUTE)
    )
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val categories = listOf("Salary", "Interest", "Gifts", "Selling", "Other")

    val saveAction = {
        val amountValue = parseNumericInput(amount)
        if (amountValue > 0 && selectedAccount != null) {
            onSave(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.INCOME,
                    fromAccountId = selectedAccount!!.id,
                    amount = amountValue,
                    payee = payee,
                    description = description,
                    memo = memo,
                    date = transactionDate,
                    status = status
                )
            )
        }
    }

    LaunchedEffect(amount, payee, description, memo, selectedAccount, transactionDate, status) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Income",
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
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                decorationBox = { inner ->
                    if (amount.isEmpty()) Text("0,00", color = Color.LightGray, fontSize = 18.sp)
                    inner()
                }
            )
            Text("USD", color = Color.Gray, fontSize = 16.sp)
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

        Row(modifier = Modifier.fillMaxWidth().clickable { showCategoryPicker = true }) {
            Row(modifier = Modifier.weight(1f).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Folder, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (category.isEmpty()) "Select Category" else category,
                    color = if (category.isEmpty()) Color.LightGray else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            Box(modifier = Modifier.width(0.5.dp).height(56.dp).background(Color(0xFFF2F2F7)))
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Split", color = Color.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Outlined.FilterList, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
        TransactionDivider()

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.BookmarkBorder, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            TransactionStatusToggle("Cleared", status == TransactionStatus.CLEARED) { status = TransactionStatus.CLEARED }
            Spacer(modifier = Modifier.width(8.dp))
            TransactionStatusToggle("Pending", status == TransactionStatus.PENDING) { status = TransactionStatus.PENDING }
        }
        TransactionDivider()

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

    if (showAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
            PickerSheetContent("Select Account", allAccounts, { it.name }, { it.type.icon }, { it.color }, {
                selectedAccount = it
                showAccountPicker = false
            })
        }
    }

    if (showCategoryPicker) {
        ModalBottomSheet(onDismissRequest = { showCategoryPicker = false }) {
            PickerSheetContent("Select Category", categories, { it }, { Icons.Default.Folder }, { Color.Gray }, {
                category = it
                showCategoryPicker = false
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
