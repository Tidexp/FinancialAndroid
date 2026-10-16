package com.example.financial.presentation.screen.transactions.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.component.*
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    showHeader: Boolean = true,
    fromAccount: Account? = null,
    allAccounts: List<Account> = emptyList(),
    existingTransaction: Transaction? = null,
    onSave: (Transaction) -> Unit = {},
    onCloseClick: () -> Unit = {},
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    var selectedFromAccount by remember { mutableStateOf(fromAccount ?: allAccounts.find { it.id == existingTransaction?.fromAccountId }) }
    var selectedToAccount by remember { mutableStateOf<Account?>(allAccounts.find { it.id == existingTransaction?.toAccountId }) }
    var amount by remember { mutableStateOf(existingTransaction?.amount?.let { if (it > 0) String.format(Locale.getDefault(), "%.2f", it) else "" } ?: "") }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }
    var memo by remember { mutableStateOf(existingTransaction?.memo ?: "") }
    
    var showToAccountPicker by remember { mutableStateOf(false) }

    val saveAction = {
        val amountValue = parseNumericInput(amount)
        if (amountValue > 0 && selectedFromAccount != null && selectedToAccount != null) {
            onSave(
                Transaction(
                    id = existingTransaction?.id ?: UUID.randomUUID().toString(),
                    type = TransactionType.TRANSFER,
                    fromAccountId = selectedFromAccount!!.id,
                    toAccountId = selectedToAccount!!.id,
                    amount = amountValue,
                    description = description,
                    memo = memo,
                    date = existingTransaction?.date ?: System.currentTimeMillis(),
                    status = existingTransaction?.status ?: TransactionStatus.CLEARED
                )
            )
        }
    }

    LaunchedEffect(amount, description, memo, selectedFromAccount, selectedToAccount) {
        onRegisterSaveAction(saveAction)
    }

    TransactionBaseScreen(
        title = "Transfer",
        onCloseClick = onCloseClick,
        onSaveClick = saveAction,
        showHeader = showHeader,
        typeSelector = {}
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                ExpenseItem(icon = Icons.Outlined.CreditCard, label = "From Account", value = selectedFromAccount?.name ?: "Select")
                TransactionDivider()
                ExpenseItem(icon = Icons.Outlined.CreditCard, label = "To Account", value = selectedToAccount?.name ?: "Select", onClick = { showToAccountPicker = true })
            }
            Icon(
                Icons.Default.SwapVert, 
                contentDescription = "Swap", 
                tint = Color.Gray,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).size(24.dp)
            )
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AddCircle, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                decorationBox = { inner ->
                    if (amount.isEmpty()) Text("0,00", color = Color.LightGray, fontSize = 18.sp)
                    inner()
                }
            )
            Text("USD", color = Color.Gray, fontSize = 16.sp)
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.FontDownload, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (description.isEmpty()) Text("Description", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
        }
        TransactionDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Description, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = memo,
                onValueChange = { memo = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black),
                decorationBox = { inner ->
                    if (memo.isEmpty()) Text("Memo", color = Color.LightGray, fontSize = 16.sp)
                    inner()
                }
            )
        }
    }

    if (showToAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showToAccountPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("To Account", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(allAccounts.filter { it.id != selectedFromAccount?.id }) { account ->
                        ListItem(
                            modifier = Modifier.clickable {
                                selectedToAccount = account
                                showToAccountPicker = false
                            },
                            headlineContent = { Text(account.name) }
                        )
                    }
                }
            }
        }
    }
}
