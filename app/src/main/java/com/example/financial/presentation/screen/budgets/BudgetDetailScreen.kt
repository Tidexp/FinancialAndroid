package com.example.financial.presentation.screen.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Budget
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    budget: Budget,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val isIncome = budget.isIncome
    
    // Calculate daily info
    val now = System.currentTimeMillis()
    val periodMillis: Long = when (budget.frequencyUnit.lowercase()) {
        "day" -> 24L * 60 * 60 * 1000
        "week" -> 7L * 24 * 60 * 60 * 1000
        "month" -> 30L * 24 * 60 * 60 * 1000
        "year" -> 365L * 24 * 60 * 60 * 1000
        else -> 30L * 24 * 60 * 60 * 1000
    }
    val timePassed = now - budget.startDate
    val periodsPassed = if (timePassed > 0) (timePassed / periodMillis).toInt() else 0
    val currentPeriodStart = budget.startDate + (periodsPassed * periodMillis)
    val currentPeriodEnd = currentPeriodStart + periodMillis
    val remainingMillis = currentPeriodEnd - now
    val remainingDays = (remainingMillis / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
    
    val dailyValue = budget.remaining / remainingDays

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
                    IconButton(
                        onClick = onAddTransactionClick,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = budget.color.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isIncome) "Income Progress" else "Expense Progress",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        String.format(Locale.getDefault(), "$%.2f", budget.spent),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = budget.color
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { budget.progress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                        color = budget.color,
                        trackColor = budget.color.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        String.format(Locale.getDefault(), "%.1f%% of $%.2f", budget.progress * 100, budget.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(
                    label = if (isIncome) "Saved" else "Used",
                    value = String.format(Locale.getDefault(), "$%.2f", budget.spent),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Remaining",
                    value = String.format(Locale.getDefault(), "$%.2f", budget.remaining),
                    color = if (budget.remaining < 0) Color.Red else budget.color,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(
                    label = "Budgeted",
                    value = String.format(Locale.getDefault(), "$%.2f", budget.amount),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = if (isIncome) "Daily Goal" else "Daily Allowance",
                    value = String.format(Locale.getDefault(), "$%.2f", dailyValue),
                    modifier = Modifier.weight(1f)
                )
            }

            if (budget.rolloverEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Rollover is enabled and automatically applied to your remaining balance.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(modifier: Modifier = Modifier, label: String, value: String, color: Color = Color.Black) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
