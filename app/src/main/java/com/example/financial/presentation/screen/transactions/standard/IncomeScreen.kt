package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import java.util.UUID

@Composable
fun IncomeScreen(
    showHeader: Boolean = true,
    account: Account? = null,
    onSave: (Transaction) -> Unit = {},
    onCloseClick: () -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var payee by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var isAutoPay by remember { mutableStateOf(false) }

    val saveAction = {
        val amountValue = parseNumericInput(amount)
        if (amountValue > 0 && account != null) {
            onSave(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.INCOME,
                    fromAccountId = account.id,
                    amount = amountValue,
                    payee = payee,
                    description = description,
                    memo = memo,
                    status = TransactionStatus.CLEARED
                )
            )
        }
    }

    LaunchedEffect(amount, payee, description, memo, account) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Income",
        onCloseClick = onCloseClick,
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {}
    ) {
        ExpenseItem(icon = Icons.Outlined.CreditCard, label = "Select Account", value = account?.name ?: "Select")
        TransactionDivider()

        TransactionRow(
            icon = Icons.Outlined.AddCircle,
            label = if (amount.isEmpty()) "0,00" else amount,
            trailingText = "USD",
            hasArrow = true,
            onClick = { }
        )
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

        Row(modifier = Modifier.fillMaxWidth().clickable { }) {
            Row(modifier = Modifier.weight(1f).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Folder, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Select Category", color = Color.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
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

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            TransactionDateBadge("5 Jul 2026")
            Spacer(modifier = Modifier.width(8.dp))
            TransactionDateBadge("12:00")
            Spacer(modifier = Modifier.weight(1f))
            Text("Date & time", color = Color.Gray, fontSize = 14.sp)
        }
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ScreenshotMonitor, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Auto-pay", color = Color.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isAutoPay,
                onCheckedChange = { isAutoPay = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3478F6))
            )
        }
        TransactionDivider()

        ExpenseItem(icon = Icons.Outlined.Repeat, label = "Repeat", value = "Every month")
        TransactionDivider()
        
        ExpenseItem(icon = Icons.AutoMirrored.Outlined.EventNote, label = "Weekends", value = "No change")
        TransactionDivider()

        ExpenseItem(icon = Icons.Outlined.StopCircle, label = "End", value = "Never")
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
}
