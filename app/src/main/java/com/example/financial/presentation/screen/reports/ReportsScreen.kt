package com.example.financial.presentation.screen.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import com.example.financial.BuildConfig
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Budget
import com.example.financial.domain.model.Transaction
import com.example.financial.domain.model.TransactionType
import com.example.financial.presentation.viewmodel.FinancialViewModel
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
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
fun ReportsScreen(
    viewModel: FinancialViewModel,
    onOpenAiChat: () -> Unit = {}
) {
    val uiState by viewModel.homeUiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(TimePeriod.THIS_MONTH) }
    var selectedCategoryTab by remember { mutableIntStateOf(0) } // 0: Overview & AI, 1: Accounts Source, 2: Budgets Breakdown, 3: Charts & Trends

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

    val categoryExpenses = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy {
                val cat = it.categoryId?.ifBlank { null }
                val payeeName = it.payee?.ifBlank { null }
                val desc = it.description?.ifBlank { null }
                cat ?: payeeName ?: desc ?: "General Expense"
            }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }
            .entries.sortedByDescending { it.value }
    }

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

            // Category Navigation Tabs (Overview, Accounts, Budgets, Charts)
            item {
                @Suppress("DEPRECATION")
                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryTab,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedCategoryTab == 0,
                        onClick = { selectedCategoryTab = 0 },
                        text = { Text("Overview & AI") }
                    )
                    Tab(
                        selected = selectedCategoryTab == 1,
                        onClick = { selectedCategoryTab = 1 },
                        text = { Text("Accounts Source") }
                    )
                    Tab(
                        selected = selectedCategoryTab == 2,
                        onClick = { selectedCategoryTab = 2 },
                        text = { Text("Budgets Status") }
                    )
                    Tab(
                        selected = selectedCategoryTab == 3,
                        onClick = { selectedCategoryTab = 3 },
                        text = { Text("Charts & Trends") }
                    )
                }
            }

            // TAB 0: OVERVIEW & AI ADVICE
            if (selectedCategoryTab == 0) {
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

                // Enhanced AI Financial Advisor Card (Receives Accounts + Budgets + Transactions)
                item {
                    AiAdviceCard(
                        periodLabel = selectedPeriod.label,
                        income = totalIncome,
                        expense = totalExpense,
                        netCashflow = netCashflow,
                        savingsRate = savingsRate,
                        categoryExpenses = categoryExpenses,
                        accounts = uiState.accounts,
                        budgets = uiState.budgets,
                        transactions = filteredTransactions,
                        onOpenAiChat = onOpenAiChat
                    )
                }
            }

            // TAB 1: ACCOUNTS ANALYTICS & SOURCE OF CHANGE
            if (selectedCategoryTab == 1) {
                item {
                    Text(
                        "Accounts Analytics & Source of Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.accounts) { account ->
                    AccountSourceAnalyticsCard(
                        account = account,
                        transactions = filteredTransactions
                    )
                }
            }

            // TAB 2: BUDGETS BREAKDOWN & SOURCE OF CHANGE
            if (selectedCategoryTab == 2) {
                item {
                    Text(
                        "Budgets Performance & Transaction Sources",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.budgets.isEmpty()) {
                    item {
                        EmptyChartPlaceholder("No budgets set up yet. Add budgets to see analytics!")
                    }
                } else {
                    items(uiState.budgets) { budget ->
                        BudgetSourceAnalyticsCard(
                            budget = budget,
                            allTransactions = filteredTransactions,
                            allAccounts = uiState.accounts
                        )
                    }
                }
            }

            // TAB 3: CHARTS & TRENDS
            if (selectedCategoryTab == 3) {
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
                            CashflowBarChart(transactions = filteredTransactions)
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(24.dp))
                            CategoryDonutChart(categoryExpenses = categoryExpenses)
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(24.dp))
                            NetWorthTrendChart(transactions = filteredTransactions)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSourceAnalyticsCard(
    account: Account,
    transactions: List<Transaction>
) {
    var expanded by remember { mutableStateOf(false) }

    val accountTransactions = remember(transactions, account.id) {
        transactions.filter { it.fromAccountId == account.id || it.toAccountId == account.id }
    }

    val accountIncome = remember(accountTransactions) {
        accountTransactions.filter {
            (it.type == TransactionType.INCOME && it.fromAccountId == account.id) ||
            (it.type == TransactionType.TRANSFER && it.toAccountId == account.id)
        }.sumOf { it.amount }
    }

    val accountExpense = remember(accountTransactions) {
        accountTransactions.filter {
            (it.type == TransactionType.EXPENSE && it.fromAccountId == account.id) ||
            (it.type == TransactionType.TRANSFER && it.fromAccountId == account.id)
        }.sumOf { it.amount }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(account.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(account.balance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = account.color)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Inflow: +$${String.format(Locale.getDefault(), "%.2f", accountIncome)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Outflow: -$${String.format(Locale.getDefault(), "%.2f", accountExpense)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("Source Transactions (${accountTransactions.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (accountTransactions.isEmpty()) {
                    Text("No transactions in this account for the period.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    accountTransactions.take(5).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tx.payee?.ifBlank { tx.description } ?: tx.type.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val isPositive = (tx.type == TransactionType.INCOME) || (tx.type == TransactionType.TRANSFER && tx.toAccountId == account.id)
                            Text(
                                text = (if (isPositive) "+" else "-") + String.format(Locale.getDefault(), "$%.2f", tx.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetSourceAnalyticsCard(
    budget: Budget,
    allTransactions: List<Transaction>,
    allAccounts: List<Account>
) {
    var expanded by remember { mutableStateOf(false) }

    val budgetTransactions = remember(allTransactions, budget) {
        allTransactions.filter { t ->
            if (t.budgetId == budget.id) return@filter true
            if (t.budgetId != null) return@filter false
            val typeMatch = t.type == (if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE)
            val accMatch = budget.accountIds.isEmpty() || budget.accountIds.contains(t.fromAccountId)
            val catMatch = budget.categories.isEmpty() || budget.categories.any { it.equals(t.payee, true) || it.equals(t.description, true) }
            typeMatch && accMatch && catMatch
        }
    }

    val progress = if (budget.amount > 0) (budget.spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(budget.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(budget.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f / $%.2f", budget.spent, budget.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress >= 1f) Color(0xFFE53935) else budget.color
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("Budget Transactions & Funding Sources:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (budgetTransactions.isEmpty()) {
                    Text("No transactions for this budget.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    budgetTransactions.take(5).forEach { tx ->
                        val sourceAcc = allAccounts.find { it.id == tx.fromAccountId }?.name ?: "Main Wallet"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tx.payee?.ifBlank { tx.description } ?: "Transaction", style = MaterialTheme.typography.bodyMedium)
                                Text("Source: $sourceAcc", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "$%.2f", tx.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAdviceCard(
    periodLabel: String,
    income: Double,
    expense: Double,
    netCashflow: Double,
    savingsRate: Double,
    categoryExpenses: List<Map.Entry<String, Float>>,
    accounts: List<Account> = emptyList(),
    budgets: List<Budget> = emptyList(),
    transactions: List<Transaction> = emptyList(),
    onOpenAiChat: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var aiAdviceText by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val defaultKey = BuildConfig.GEMINI_API_KEY
    var customKey by remember { mutableStateOf("") }

    fun generateAdvice() {
        val apiKey = customKey.ifBlank { defaultKey }
        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            showApiKeyDialog = true
            return
        }

        isLoadingAi = true
        coroutineScope.launch {
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-3.5-flash-lite",
                    apiKey = apiKey
                )

                val topCats = categoryExpenses.take(5).joinToString(", ") { "${it.key}: $${String.format(Locale.getDefault(), "%.2f", it.value)}" }
                val accountsSummary = accounts.joinToString("; ") { "${it.name} (${it.type.name}): ${it.balance}" }
                val budgetsSummary = budgets.joinToString("; ") { "${it.name}: $${String.format(Locale.getDefault(), "%.2f", it.spent)} spent of $${String.format(Locale.getDefault(), "%.2f", it.amount)}" }

                val prompt = """
                    You are an expert personal finance advisor AI.
                    Analyze this comprehensive financial data for the period '$periodLabel':

                    Cashflow Overview:
                    - Total Income: $${String.format(Locale.getDefault(), "%.2f", income)}
                    - Total Expenses: $${String.format(Locale.getDefault(), "%.2f", expense)}
                    - Net Cashflow: $${String.format(Locale.getDefault(), "%.2f", netCashflow)}
                    - Savings Rate: ${String.format(Locale.getDefault(), "%.1f", savingsRate)}%
                    - Top Expense Categories: $topCats

                    Accounts & Balances:
                    - $accountsSummary

                    Budgets & Utilization:
                    - $budgetsSummary

                    Provide 3 to 5 clear, encouraging, actionable financial advice bullet points covering:
                    1. Cashflow & Savings optimization
                    2. Account balance or debt health
                    3. Budget adjustments
                    Keep the response well-structured and practical.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                aiAdviceText = response.text ?: "No advice generated."
            } catch (e: Exception) {
                aiAdviceText = "Error generating AI advice: ${e.localizedMessage}"
            } finally {
                isLoadingAi = false
            }
        }
    }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Google AI Studio API Key", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your Gemini API key from Google AI Studio (aistudio.google.com):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customKey,
                        onValueChange = { customKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApiKeyDialog = false
                        generateAdvice()
                    }
                ) {
                    Text("Save & Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Financial Advisor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { showApiKeyDialog = true }) {
                    Icon(Icons.Default.Key, contentDescription = "API Key", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (aiAdviceText == null && !isLoadingAi) {
                Text(
                    "Get comprehensive financial insights analyzing Accounts, Budgets, and Cashflow powered by Google Gemini AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isLoadingAi) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analyzing Accounts, Budgets & Cashflow with Gemini AI...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (aiAdviceText != null) {
                Text(
                    text = aiAdviceText.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { generateAdvice() },
                enabled = !isLoadingAi,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (aiAdviceText == null) "Get AI Analysis" else "Refresh AI Analysis")
            }

            TextButton(
                onClick = onOpenAiChat,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with AI")
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
fun CategoryDonutChart(categoryExpenses: List<Map.Entry<String, Float>>) {
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
