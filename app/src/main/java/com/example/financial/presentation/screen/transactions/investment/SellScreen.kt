package com.example.financial.presentation.screen.transactions.investment

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
fun SellScreen(
    showHeader: Boolean = true,
    account: Account? = null,
    onSave: (Transaction) -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var symbol by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf("") }
    var pricePerShare by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var isCleared by remember { mutableStateOf(true) }

    val saveAction = {
        val s = parseNumericInput(shares)
        val p = parseNumericInput(pricePerShare)
        val c = parseNumericInput(commission)
        val totalAmount = (s * p) - c
        if (totalAmount > 0 && account != null) {
            onSave(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.SELL,
                    fromAccountId = account.id,
                    amount = totalAmount,
                    symbol = symbol,
                    shares = s,
                    pricePerShare = p,
                    commission = c,
                    memo = memo,
                    status = if (isCleared) TransactionStatus.CLEARED else TransactionStatus.PENDING
                )
            )
        }
    }

    LaunchedEffect(symbol, shares, pricePerShare, commission, memo, isCleared, account) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Sell",
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
                TransactionTypeIcon(Icons.Outlined.ArrowCircleUp, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.ArrowCircleDown, isSelected = true, selectedColor = Color.Black)
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = false)
            }
        }
    ) {
        TransactionInputRow(icon = Icons.Outlined.Search, value = symbol, onValueChange = { symbol = it }, placeholder = "Symbol")
        TransactionDivider()

        TransactionRow(Icons.Outlined.Assignment, "Asset Class", hasArrow = true, contentColor = Color(0xFF3A3A3C))
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.AddCircle, value = shares, onValueChange = { shares = it }, placeholder = "Number of shares")
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.AddCircle, value = pricePerShare, onValueChange = { pricePerShare = it }, placeholder = "Price per share")
        TransactionDivider()

        val total = (shares.toDoubleOrNull() ?: 0.0) * (pricePerShare.toDoubleOrNull() ?: 0.0)
        TransactionRow(Icons.Outlined.AddCircle, String.format("%.2f", total), middleText = "Total price", contentColor = Color.Black)
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.AddCircle, value = commission, onValueChange = { commission = it }, placeholder = "Commission")
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:43")
            Spacer(Modifier.weight(1f))
            Text("Date & time", color = Color.LightGray)
        }
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.BookmarkBorder, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionStatusToggle("Cleared", isCleared) { isCleared = true }
            Spacer(Modifier.width(8.dp))
            TransactionStatusToggle("Pending", !isCleared) { isCleared = false }
        }
        TransactionDivider()

        TransactionInputRow(icon = Icons.Outlined.Description, value = memo, onValueChange = { memo = it }, placeholder = "Memo")
    }
}
