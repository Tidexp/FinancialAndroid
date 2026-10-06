package com.example.financial.presentation.screen.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class TimePeriod(val label: String) {
    THIS_MONTH("This Month"),
    LAST_3_MONTHS("3 Months"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: FinancialViewModel) {
    val uiState by viewModel.homeUiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(TimePeriod.THIS_MONTH) }
    var selectedChartTab by remember { mutableIntStateOf(0) }

    val filteredTransactions = remember(uiState.transactions, selectedPeriod) {
        val cutoffMillis = when (selectedPeriod) {
            TimePeriod.THIS_MONTH -> Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
            TimePeriod.LAST_3_MONTHS -> Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.timeInMillis
            TimePeriod.THIS_YEAR -> Calendar.getInstance().apply { set(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
            TimePeriod.ALL_TIME -> 0L
        }
        uiState.transactions.filter { it.date >= cutoffMillis && it.budgetId == null }
    }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val netCashflow = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netCashflow / totalIncome) * 100).coerceIn(0.0, 100.0) else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(TimePeriod.entries) { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.label) }
                        )
                    }
                }
            }

            // Overview Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMetricCard(
                        title = "Income",
                        amount = String.format(Locale.getDefault(), "$%.2f", totalIncome),
                        color = Color(0xFF4CAF50),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Expenses",
                        amount = String.format(Locale.getDefault(), "$%.2f", totalExpense),
                        color = Color(0xFFE53935),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Net Cashflow", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.getDefault(), "$%.2f", netCashflow),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (netCashflow >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Savings Rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", savingsRate),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Chart Type Tab Selector
            item {
                @Suppress("DEPRECATION")
                TabRow(selectedTabIndex = selectedChartTab, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                    Tab(
                        selected = selectedChartTab == 0,
                        onClick = { selectedChartTab = 0 },
                        text = { Text("Cashflow Bar") }
                    )
                    Tab(
                        selected = selectedChartTab == 1,
                        onClick = { selectedChartTab = 1 },
                        text = { Text("Categories") }
                    )
                    Tab(
                        selected = selectedChartTab == 2,
                        onClick = { selectedChartTab = 2 },
                        text = { Text("Trend Line") }
                    )
                }
            }

            // Active Chart Display
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (selectedChartTab) {
                            0 -> CashflowBarChart(transactions = filteredTransactions)
                            1 -> CategoryDonutChart(transactions = filteredTransactions)
                            2 -> NetWorthTrendChart(transactions = filteredTransactions)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    amount: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun CashflowBarChart(transactions: List<Transaction>) {
    val monthsData = remember(transactions) {
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        val grouped = transactions.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            sdf.format(cal.time)
        }

        grouped.map { (month, txs) ->
            val inc = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }.toFloat()
            val exp = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }.toFloat()
            MonthSummary(month, inc, exp)
        }.takeLast(6)
    }

    if (monthsData.isEmpty()) {
        EmptyChartPlaceholder("No transaction data available for this period")
        return
    }

    val maxAmount = remember(monthsData) {
        monthsData.flatMap { listOf(it.income, it.expense) }.maxOrNull()?.coerceAtLeast(100f) ?: 100f
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Income vs Expense Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val barWidth = 24.dp.toPx()
            val spacing = width / (monthsData.size.coerceAtLeast(1))

            monthsData.forEachIndexed { index, data ->
                val xCenter = spacing * index + spacing / 2f
                val incomeBarHeight = (data.income / maxAmount) * (height - 30.dp.toPx())
                val expenseBarHeight = (data.expense / maxAmount) * (height - 30.dp.toPx())

                // Income Bar (Green)
                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(xCenter - barWidth - 4.dp.toPx(), height - incomeBarHeight - 20.dp.toPx()),
                    size = Size(barWidth, incomeBarHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // Expense Bar (Red)
                drawRoundRect(
                    color = Color(0xFFE53935),
                    topLeft = Offset(xCenter + 4.dp.toPx(), height - expenseBarHeight - 20.dp.toPx()),
                    size = Size(barWidth, expenseBarHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            monthsData.forEach {
                Text(it.month, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(Color(0xFF4CAF50), "Income")
            LegendItem(Color(0xFFE53935), "Expenses")
        }
    }
}

@Composable
fun CategoryDonutChart(transactions: List<Transaction>) {
    val categoryExpenses = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.payee?.ifBlank { "Uncategorized" } ?: "General" }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }
            .entries.sortedByDescending { it.value }
    }

    if (categoryExpenses.isEmpty()) {
        EmptyChartPlaceholder("No expenses recorded in this period")
        return
    }

    val totalExpense = categoryExpenses.sumOf { it.value.toDouble() }.toFloat()
    val sliceColors = listOf(
        Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF4CAF50), Color(0xFFFB8C00),
        Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFFFFB300), Color(0xFFD81B60)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Expense Breakdown by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                categoryExpenses.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value / totalExpense) * 360f
                    val color = sliceColors[index % sliceColors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 28.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = String.format(Locale.getDefault(), "$%.0f", totalExpense),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryExpenses.take(5).forEachIndexed { index, entry ->
                val percent = if (totalExpense > 0) (entry.value / totalExpense) * 100f else 0f
                val color = sliceColors[index % sliceColors.size]

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(entry.key, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f (%.1f%%)", entry.value, percent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NetWorthTrendChart(transactions: List<Transaction>) {
    val trendData = remember(transactions) {
        val sorted = transactions.sortedBy { it.date }
        var runningBalance = 0f
        val points = mutableListOf<Float>()

        sorted.forEach { tx ->
            if (tx.type == TransactionType.INCOME) runningBalance += tx.amount.toFloat()
            if (tx.type == TransactionType.EXPENSE) runningBalance -= tx.amount.toFloat()
            points.add(runningBalance)
        }
        if (points.isEmpty()) listOf(0f, 100f, 250f, 200f, 450f, 600f) else points
    }

    val minVal = trendData.minOrNull() ?: 0f
    val maxVal = trendData.maxOrNull()?.coerceAtLeast(minVal + 1f) ?: 100f
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Cumulative Balance Growth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (trendData.size - 1).coerceAtLeast(1)

            val path = Path()
            val fillPath = Path()

            trendData.forEachIndexed { index, value ->
                val x = index * spacing
                val normalizedY = (value - minVal) / (maxVal - minVal)
                val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == trendData.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }

                // Draw data points
                drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
            }

            // Draw area gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EmptyChartPlaceholder(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

data class MonthSummary(val month: String, val income: Float, val expense: Float)
