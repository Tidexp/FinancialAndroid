package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    showHeader: Boolean = true,
    fromAccount: Account? = null,
    allAccounts: List<Account> = emptyList(),
    onSave: (Transaction) -> Unit = {},
    onCloseClick: () -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var isAutoPay by remember { mutableStateOf(false) }
    
    var selectedToAccount by remember { mutableStateOf<Account?>(null) }
    var showToAccountPicker by remember { mutableStateOf(false) }

    val saveAction = {
        val amountValue = parseNumericInput(amount)
        if (amountValue > 0 && fromAccount != null && selectedToAccount != null) {
            onSave(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.TRANSFER,
                    fromAccountId = fromAccount.id,
                    toAccountId = selectedToAccount!!.id,
                    amount = amountValue,
                    description = description,
                    memo = memo,
                    status = TransactionStatus.CLEARED
                )
            )
        }
    }

    LaunchedEffect(amount, description, memo, fromAccount, selectedToAccount) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Transfer",
        onCloseClick = onCloseClick,
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {}
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                ExpenseItem(icon = Icons.Outlined.CreditCard, label = "Select Account", value = fromAccount?.name ?: "Select")
                TransactionDivider()
                ExpenseItem(icon = Icons.Outlined.CreditCard, label = "To Account", value = selectedToAccount?.name ?: "Select", onClick = { showToAccountPicker = true })
            }
            Icon(
                Icons.Default.SwapVert, 
                contentDescription = "Swap", 
                tint = Color.Gray,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).size(24.dp)
            )
        }
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
        TransactionDivider()
        
        ExpenseItem(icon = Icons.Outlined.LocalOffer, label = "Tags", value = "")
    }

    if (showToAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showToAccountPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("To Account", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(allAccounts.filter { it.id != fromAccount?.id }) { account ->
                        ListItem(
                            modifier = Modifier.clickable {
                                selectedToAccount = account
                                showToAccountPicker = false
                            },
                            headlineContent = { Text(account.name) }
                        )
                    }
                }
            }
        }
    }
}
