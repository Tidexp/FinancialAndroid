package com.example.financial.presentation.screen.transactions.crypto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financial.presentation.component.*

@Composable
fun ExchangeScreen(showHeader: Boolean = true) {
    var isCleared by remember { mutableStateOf(true) }

    TransactionBaseScreen(
        title = "Exchange",
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
                TransactionTypeIcon(Icons.Outlined.Cached, isSelected = true, selectedColor = Color.Black)
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = false)
            }
        }
    ) {
        TransactionRow(Icons.Outlined.AddCircle, "From", trailingText = "Select", contentColor = Color.LightGray)
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SwapVert, null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            Text("1,00000000", color = Color.Black, modifier = Modifier.weight(1f))
            Text("Exchange rate", color = Color.LightGray, modifier = Modifier.padding(end = 12.dp))
            Icon(
                Icons.Outlined.Cached,
                null,
                tint = Color(0xFF3478F6),
                modifier = Modifier.size(20.dp).clickable { }
            )
        }
        TransactionDivider()

        TransactionRow(Icons.Outlined.AddCircle, "0,00", middleText = "To", trailingText = "Select")
        TransactionDivider()

        TransactionRow(Icons.Outlined.AddCircle, "0,00", middleText = "Commission", trailingText = "Select", contentColor = Color.LightGray)
        TransactionDivider()

        TransactionRow(Icons.Outlined.AddCircle, "0,00", middleText = "Total", trailingText = "Select", contentColor = Color.LightGray)
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

        TransactionRow(Icons.Outlined.FontDownload, "Description", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        TransactionRow(Icons.Outlined.Description, "Memo", contentColor = Color.LightGray)
        TransactionDivider()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Outlined.Image, null, tint = Color.Gray)
            Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF3478F6))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExchange() {
    ExchangeScreen()
}
