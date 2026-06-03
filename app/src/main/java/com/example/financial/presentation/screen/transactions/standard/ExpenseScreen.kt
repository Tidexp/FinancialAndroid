package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SwapHoriz
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
fun ExpenseScreen(showHeader: Boolean = true) {
    var isCleared by remember { mutableStateOf(true) }

    TransactionBaseScreen(
        title = "Expense",
        onCloseClick = { },
        onSaveClick = { },
        showHeader = showHeader,
        typeSelector = {
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = true)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.Calculate, isSelected = false)
            }
        }
    ) {
        TransactionRow(icon = Icons.Outlined.CreditCard, label = "Select Account", hasArrow = true)
        TransactionDivider()

        TransactionRow(
            icon = Icons.Outlined.AddCircle,
            label = "0,00",
            trailingText = "USD",
            hasArrow = true,
            contentColor = Color.LightGray
        )
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.Person, label = "Payee", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.FontDownload, label = "Description", hasArrow = true, contentColor = Color.LightGray)
        TransactionDivider()

        // Category & Split row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Folder, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Select Category", color = Color.Black, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
            Box(modifier = Modifier.width(1.dp).height(48.dp).background(Color(0xFFE5E5EA)))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Split ", color = Color.Black)
                Icon(Icons.Outlined.FilterAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
        TransactionDivider()

        // Date & Time
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                TransactionDateBadge(text = "2 Jun 2026")
                Spacer(modifier = Modifier.width(8.dp))
                TransactionDateBadge(text = "14:16")
            }
            Text(text = "Date & time", color = Color.LightGray)
        }
        TransactionDivider()

        // Status
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            TransactionStatusToggle(text = "Cleared", isSelected = isCleared) { isCleared = true }
            Spacer(modifier = Modifier.width(8.dp))
            TransactionStatusToggle(text = "Pending", isSelected = !isCleared) { isCleared = false }
        }
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.Description, label = "Memo", contentColor = Color.LightGray)
        TransactionDivider()

        // Images
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Gray)
            Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = Color(0xFF3478F6))
        }
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.LocalOffer, label = "Tags", hasArrow = true, contentColor = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    ExpenseScreen()
}
