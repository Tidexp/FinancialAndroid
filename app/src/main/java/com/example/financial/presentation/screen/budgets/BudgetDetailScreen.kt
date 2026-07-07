package com.example.financial.presentation.screen.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Budget
import com.example.financial.domain.model.Transaction
import com.example.financial.presentation.component.TransactionDetailSheet
import com.example.financial.presentation.component.TransactionItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    budget: Budget,
    transactions: List<Transaction>,
    allAccounts: List<com.example.financial.domain.model.Account> = emptyList(),
    allBudgets: List<Budget> = emptyList(),
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val now = System.currentTimeMillis()
    val periodMillis: Long = when (budget.frequencyUnit.lowercase()) {
        "day" -> 24L * 3600000
        "week" -> 7L * 24 * 3600000
        "year" -> 365L * 24 * 3600000
        else -> 30L * 24 * 3600000
    }

    val timePassed = now - budget.startDate
    val periodsPassed = if (timePassed > 0) (timePassed / periodMillis).toInt() else 0
    val currentPeriodStart = budget.startDate + (periodsPassed * periodMillis)
    val currentPeriodEnd = currentPeriodStart + periodMillis
    
    val remainingMillis = currentPeriodEnd - now
    val remainingDays = (remainingMillis / (24 * 3600000)).coerceAtLeast(1)
    
    val dailyValue = if (remainingDays > 0) budget.remaining / remainingDays else 0.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(budget.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddTransactionClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Entry")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Circle
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { budget.progress },
                        modifier = Modifier.fillMaxSize(),
                        color = budget.color,
                        strokeWidth = 12.dp,
                        trackColor = budget.color.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f%%", budget.progress * 100),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (budget.isIncome) "Saved" else "Used",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val label1 = if (budget.isIncome) "Saved" else "Used"
                        val label4 = if (budget.isIncome) "Daily goal" else "Daily Allowance"
                        
                        BudgetDetailRow(label1, formatCurrency(budget.spent))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        BudgetDetailRow("Remaining", formatCurrency(budget.remaining), isBold = true, color = if (budget.remaining < 0) Color.Red else Color(0xFF4CAF50))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        BudgetDetailRow("Budgeted", formatCurrency(budget.amount))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        BudgetDetailRow(label4, formatCurrency(dailyValue))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "$remainingDays days remaining in this period",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "TRANSACTION HISTORY",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "No transactions in this period.",
                        modifier = Modifier.padding(vertical = 48.dp),
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(transactions) { transaction ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TransactionItem(
                            transaction = transaction,
                            onClick = { selectedTransaction = transaction }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailSheet(
            transaction = transaction,
            accounts = allAccounts,
            budgets = allBudgets,
            onDismiss = { selectedTransaction = null }
        )
    }
}

@Composable
fun BudgetDetailRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Text(text = value, color = color, fontSize = 16.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format(Locale.getDefault(), "$%.2f", amount)
}
