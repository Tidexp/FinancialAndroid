package com.example.financial.presentation.screen.transactions.crypto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financial.presentation.component.*

@Composable
fun CryptoTransferScreen(showHeader: Boolean = true) {
    var isCleared by remember { mutableStateOf(true) }

    TransactionBaseScreen(
        title = "Transfer",
        onCloseClick = { },
        onSaveClick = { },
        showHeader = showHeader,
        typeSelector = {
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionTypeIcon(Icons.Outlined.Cached, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = true, selectedColor = Color.Black)
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = false)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                TransactionRow(Icons.Outlined.CreditCard, "Crypto (-$2,73)", hasArrow = true, contentColor = Color.Black)
                TransactionDivider()
                TransactionRow(Icons.Outlined.CreditCard, "To Account", hasArrow = true, contentColor = Color.LightGray)
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 36.dp)
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(50))
                    .size(28.dp)
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }
        TransactionDivider()

        TransactionRow(Icons.Outlined.AddCircle, "0,00", middleText = "Sent", trailingText = "USD", hasArrow = true, contentColor = Color.Black)
        TransactionDivider()

        TransactionRow(Icons.Outlined.AddCircle, "0,00", middleText = "Transfer Fee", trailingText = "USD", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        TransactionRow(Icons.Outlined.AddCircle, "0,00", middleText = "Received", trailingText = "USD", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        TransactionRow(Icons.Outlined.FontDownload, "Description", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:53")
            Spacer(Modifier.weight(1f))
            Text("Send date", color = Color.LightGray)
        }
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarToday, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:53")
            Spacer(Modifier.weight(1f))
            Text("Receive date", color = Color.LightGray)
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

        TransactionRow(Icons.Outlined.Description, "Memo", contentColor = Color.LightGray)
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Outlined.Image, null, tint = Color.Gray)
            Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF3478F6))
        }
        TransactionDivider()

        TransactionRow(Icons.Outlined.LocalOffer, "Tags", hasArrow = true, contentColor = Color.LightGray)
    }
}
