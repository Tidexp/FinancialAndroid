package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import java.util.UUID

@Composable
fun TransferScreen(
    showHeader: Boolean = true,
    fromAccount: Account? = null,
    allAccounts: List<Account> = emptyList(),
    onSave: (Transaction) -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var isCleared by remember { mutableStateOf(true) }
    
    var selectedToAccount by remember { mutableStateOf<Account?>(null) }
    var showAccountPicker by remember { mutableStateOf(false) }

    val saveAction = {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
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
                    status = if (isCleared) TransactionStatus.CLEARED else TransactionStatus.PENDING
                )
            )
        }
    }

    LaunchedEffect(amount, description, memo, isCleared, fromAccount, selectedToAccount) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Transfer",
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
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = true, selectedColor = Color.Black)
                TransactionTypeIcon(Icons.Outlined.Calculate, isSelected = false)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                TransactionRow(Icons.Outlined.CreditCard, fromAccount?.name ?: "Select Account", hasArrow = true)
                TransactionDivider()
                TransactionRow(
                    icon = Icons.Outlined.CreditCard, 
                    label = selectedToAccount?.name ?: "To Account", 
                    hasArrow = true,
                    contentColor = if (selectedToAccount == null) Color.LightGray else Color.Black
                )
            }

            IconButton(
                onClick = { /* Swap logic */ },
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
            
            // To Account Selector (Clickable overlay on the second row)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 56.dp) // Height of first row
                    .height(56.dp)
                    .clickable { showAccountPicker = true }
            )
        }
        TransactionDivider()

        TransactionInputRow(
            icon = Icons.Outlined.AddCircle,
            value = amount,
            onValueChange = { amount = it },
            placeholder = "0,00",
            trailingText = "USD"
        )
        TransactionDivider()

        TransactionInputRow(
            icon = Icons.Outlined.FontDownload,
            value = description,
            onValueChange = { description = it },
            placeholder = "Description"
        )
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

        TransactionInputRow(
            icon = Icons.Outlined.Description,
            value = memo,
            onValueChange = { memo = it },
            placeholder = "Memo"
        )
        TransactionDivider()

        // Attachment
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Outlined.Image, null, tint = Color.Gray)
            Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF3478F6))
        }
        TransactionDivider()

        TransactionRow(icon = Icons.Outlined.LocalOffer, label = "Tags", hasArrow = true, contentColor = Color.LightGray)
    }

    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("Select Target Account") },
            text = {
                LazyColumn {
                    items(allAccounts.filter { it.id != fromAccount?.id }) { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                selectedToAccount = account
                                showAccountPicker = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountPicker = false }) { Text("Cancel") }
            }
        )
    }
}
