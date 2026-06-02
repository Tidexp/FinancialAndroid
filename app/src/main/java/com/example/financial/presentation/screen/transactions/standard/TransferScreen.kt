package com.example.financial.presentation.screen.transactions.standard

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
fun TransferScreen() {
    var isCleared by remember { mutableStateOf(true) }

    TransactionBaseScreen(
        title = "Transfer",
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
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = true, selectedColor = Color.Black)
                TransactionTypeIcon(Icons.Outlined.Calculate, isSelected = false)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                TransactionRow(Icons.Outlined.CreditCard, "Select Account", hasArrow = true)
                TransactionDivider()
                TransactionRow(Icons.Outlined.CreditCard, "To Account", hasArrow = true)
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 36.dp)
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(50))
                    .size(28.dp)
            ) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = "Swap Accounts",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        TransactionDivider()

        TransactionRow(
            icon = Icons.Outlined.AddCircle,
            label = "0,00",
            middleText = "Transfer amount",
            trailingText = "USD",
            hasArrow = true,
            contentColor = Color.LightGray
        )
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.FontDownload, label = "Description", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        // Send date
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:16")
            Spacer(Modifier.weight(1f))
            Text("Send date", color = Color.LightGray)
        }
        TransactionDivider()

        // Receive date
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarToday, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionDateBadge("2 Jun 2026")
            Spacer(Modifier.width(8.dp))
            TransactionDateBadge("14:16")
        }
        TransactionDivider()

        // Status
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.BookmarkBorder, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            TransactionStatusToggle("Cleared", isCleared) { isCleared = true }
            Spacer(Modifier.width(8.dp))
            TransactionStatusToggle("Pending", !isCleared) { isCleared = false }
        }
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.Description, label = "Memo", contentColor = Color.LightGray)
        TransactionDivider()

        // Attachment
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Outlined.Image, null, tint = Color.Gray)
            Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF3478F6))
        }
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.LocalOffer, label = "Tags", hasArrow = true, contentColor = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun TransferScreenPreview() {
    TransferScreen()
}
