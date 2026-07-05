package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import java.util.UUID

@Composable
fun AdjustBalanceScreen(
    showHeader: Boolean = true,
    account: Account? = null,
    onSave: (Transaction) -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var newBalance by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    val saveAction = {
        val balanceValue = if (newBalance.isBlank()) {
            parseNumericInput(account?.balance ?: "0,00")
        } else {
            parseNumericInput(newBalance)
        }

        if (account != null) {
            onSave(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.ADJUSTMENT,
                    fromAccountId = account.id,
                    amount = balanceValue,
                    memo = memo,
                    status = TransactionStatus.CLEARED
                )
            )
        }
    }

    LaunchedEffect(newBalance, memo, account) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Adjust Balance",
        onCloseClick = { },
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {}
    ) {
        ExpenseItem(icon = Icons.Outlined.CreditCard, label = account?.name ?: "Select Account", value = "")
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AddCircle, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = account?.balance ?: "0,00", color = Color.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "USD", color = Color.Gray)
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = newBalance,
                onValueChange = { newBalance = it },
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (newBalance.isEmpty()) Text("New balance", color = Color.LightGray, fontSize = 16.sp)
                    innerTextField()
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = memo,
                onValueChange = { memo = it },
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (memo.isEmpty()) Text("Memo", color = Color.LightGray, fontSize = 16.sp)
                    innerTextField()
                }
            )
        }
    }
}
