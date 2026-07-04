package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        val balanceValue = newBalance.toDoubleOrNull() ?: 0.0
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
        typeSelector = {
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = true, selectedColor = Color.Black)
            }
        }
    ) {
        TransactionRow(icon = Icons.Outlined.CreditCard, label = account?.name ?: "Select Account", hasArrow = true)
        TransactionDivider()

        TransactionRow(
            icon = Icons.Outlined.AddCircle,
            label = account?.balance ?: "0,00",
            trailingText = "USD",
            hasArrow = true,
            contentColor = Color(0xFF8E8E93)
        )
        TransactionDivider()

        TransactionInputRow(
            icon = Icons.Outlined.FontDownload,
            value = newBalance,
            onValueChange = { newBalance = it },
            placeholder = "New balance",
            trailingText = "USD"
        )
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:16")
            Spacer(Modifier.weight(1f))
            Text("Date & time", color = Color.LightGray)
        }
        TransactionDivider()

        TransactionInputRow(
            icon = Icons.Outlined.Description,
            value = memo,
            onValueChange = { memo = it },
            placeholder = "Memo"
        )
    }
}
