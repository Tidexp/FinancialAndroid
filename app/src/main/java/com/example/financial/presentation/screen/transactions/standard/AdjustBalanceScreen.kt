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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financial.presentation.component.*

@Composable
fun AdjustBalanceScreen() {
    TransactionBaseScreen(
        title = "Adjust Balance",
        onCloseClick = { },
        onSaveClick = { },
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
        TransactionRow(Icons.Outlined.CreditCard, "Select Account", hasArrow = true)
        TransactionDivider()

        TransactionRow(
            Icons.Outlined.AddCircle,
            "0,00",
            trailingText = "USD",
            hasArrow = true,
            contentColor = Color(0xFF8E8E93)
        )
        TransactionDivider()

        TransactionRow(
            Icons.Outlined.FontDownload,
            "New balance",
            hasArrow = true,
            contentColor = Color(0xFF3A3A3C)
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

        TransactionRow(icon = Icons.Outlined.Description, label = "Memo", contentColor = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdjustBalance() {
    AdjustBalanceScreen()
}
