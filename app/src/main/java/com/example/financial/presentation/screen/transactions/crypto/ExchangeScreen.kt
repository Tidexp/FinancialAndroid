package com.example.financial.presentation.screen.transactions.crypto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.*
import com.example.financial.presentation.component.*
import java.util.UUID

@Composable
fun ExchangeScreen(
    showHeader: Boolean = true,
    account: Account? = null,
    onSave: (Transaction) -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var fromAmount by remember { mutableStateOf("") }
    var toAmount by remember { mutableStateOf("") }
    var exchangeRate by remember { mutableStateOf("1.0") }
    var commission by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var isCleared by remember { mutableStateOf(true) }

    val saveAction = {
        val amountValue = fromAmount.toDoubleOrNull() ?: 0.0
        if (amountValue > 0 && account != null) {
            onSave(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.EXCHANGE,
                    fromAccountId = account.id,
                    amount = amountValue,
                    exchangeRate = exchangeRate.toDoubleOrNull(),
                    commission = commission.toDoubleOrNull(),
                    description = description,
                    memo = memo,
                    status = if (isCleared) TransactionStatus.CLEARED else TransactionStatus.PENDING
                )
            )
        }
    }

    LaunchedEffect(fromAmount, toAmount, exchangeRate, commission, description, memo, isCleared, account) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Exchange",
        onCloseClick = { },
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionTypeIcon(Icons.Outlined.Cached, isSelected = true, selectedColor = Color.Black)
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = false)
            }
        }
    ) {
        TransactionInputRow(icon = Icons.Outlined.AddCircle, value = fromAmount, onValueChange = { fromAmount = it }, placeholder = "From", trailingText = account?.currency ?: "USD")
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SwapVert, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = exchangeRate,
                onValueChange = { exchangeRate = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(color = Color.Black)
            )
            Text("Exchange rate", color = Color.LightGray, modifier = Modifier.padding(end = 12.dp))
            Icon(
                Icons.Outlined.Cached,
                null,
                tint = Color(0xFF3478F6),
                modifier = Modifier.size(20.dp).clickable { }
            )
        }
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.AddCircle, value = toAmount, onValueChange = { toAmount = it }, placeholder = "0,00", trailingText = "Target")
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.AddCircle, value = commission, onValueChange = { commission = it }, placeholder = "0,00", middleText = "Commission")
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:43")
            Spacer(Modifier.weight(1f))
            Text("Date & time", color = Color.LightGray)
        }
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.BookmarkBorder, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionStatusToggle("Cleared", isCleared) { isCleared = true }
            Spacer(Modifier.width(8.dp))
            TransactionStatusToggle("Pending", !isCleared) { isCleared = false }
        }
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.FontDownload, value = description, onValueChange = { description = it }, placeholder = "Description", hasArrow = true)
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.Description, value = memo, onValueChange = { memo = it }, placeholder = "Memo")
    }
}
