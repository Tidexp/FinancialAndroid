package com.example.financial.presentation.screen.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
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

@Immutable
data class AccountReportSummary(
    val transactions: List<Transaction> = emptyList(),
    val income: Double = 0.0,
    val expense: Double = 0.0
)

@Immutable
data class BudgetReportSummary(
    val transactions: List<Transaction> = emptyList()
)

private data class MutableAccountSummary(
    val transactions: MutableList<Transaction> = mutableListOf(),
    var income: Double = 0.0,
    var expense: Double = 0.0
)

private data class MutableBudgetSummary(
    val transactions: MutableList<Transaction> = mutableListOf()
)

private data class MutableMonthSummary(
    var income: Float = 0f,
    var expense: Float = 0f
)

@Immutable
private data class ReportSummary(
    val income: Double,
    val expense: Double,
    val categoryExpenses: List<Map.Entry<String, Float>>,
    val accountReports: Map<String, AccountReportSummary>,
    val budgetReports: Map<String, BudgetReportSummary>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FinancialViewModel,
    onOpenAiChat: () -> Unit = {}
) {
    val currentLocale = LocalLocale.current.platformLocale
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val reportTransactions by viewModel.reportTransactions.collectAsStateWithLifecycle()
    var selectedPeriod by rememberSaveable { mutableStateOf(TimePeriod.THIS_MONTH) }
    var selectedCategoryTab by rememberSaveable { mutableIntStateOf(0) } // 0: Overview & AI, 1: Accounts Source, 2: Budgets Breakdown, 3: Charts & Trends

    // Elevating AI advice state to parent level to cache generated responses across tab navigation
    var cachedAiAdviceText by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoadingAi by rememberSaveable { mutableStateOf(false) }
    var customKey by rememberSaveable { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val defaultKey = BuildConfig.GEMINI_API_KEY

    val cutoffMillis = remember(selectedPeriod) {
        when (selectedPeriod) {
            TimePeriod.THIS_MONTH -> Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
            TimePeriod.LAST_3_MONTHS -> Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.timeInMillis
            TimePeriod.THIS_YEAR -> Calendar.getInstance().apply { set(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
            TimePeriod.ALL_TIME -> 0L
        }
    }

    LaunchedEffect(cutoffMillis) {
        viewModel.loadReportTransactions(cutoffMillis)
    }

    val filteredTransactions = remember(reportTransactions) { reportTransactions }

    val reportSummary = remember(filteredTransactions, uiState.budgets) {
        var income = 0.0
        var expense = 0.0
        val categoryTotals = linkedMapOf<String, Float>()

        val accountSummaries = mutableMapOf<String, MutableAccountSummary>()
        val budgetSummaries = mutableMapOf<String, MutableBudgetSummary>()

        filteredTransactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> income += transaction.amount
                TransactionType.EXPENSE -> expense += transaction.amount
                else -> Unit
            }

            if (transaction.type == TransactionType.EXPENSE) {
                val category = transaction.categoryId?.ifBlank { null }
                    ?: transaction.payee?.ifBlank { null }
                    ?: transaction.description?.ifBlank { null }
                    ?: "General Expense"
                categoryTotals[category] =
                    (categoryTotals[category] ?: 0f) + transaction.amount.toFloat()
            }

            val fromAccountId = transaction.fromAccountId
            val accountFrom = accountSummaries.getOrPut(fromAccountId) { MutableAccountSummary() }
            accountFrom.transactions.add(transaction)
            if (transaction.type == TransactionType.INCOME) {
                accountFrom.income += transaction.amount
            } else if (transaction.type == TransactionType.EXPENSE) {
                accountFrom.expense += transaction.amount
            } else if (transaction.type == TransactionType.TRANSFER) {
                accountFrom.expense += transaction.amount
            }

            transaction.toAccountId?.let { toAccountId ->
                val accountTo = accountSummaries.getOrPut(toAccountId) { MutableAccountSummary() }
                accountTo.transactions.add(transaction)
                if (transaction.type == TransactionType.TRANSFER) {
                    accountTo.income += transaction.amount
                }
            }

            uiState.budgets.forEach { budget ->
                if (transaction.budgetId == budget.id) {
                    budgetSummaries.getOrPut(budget.id) { MutableBudgetSummary() }
                        .transactions.add(transaction)
                    return@forEach
                }
                if (transaction.budgetId != null) return@forEach

                val typeMatches =
                    transaction.type == if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                val accountMatches =
                    budget.accountIds.isEmpty() || budget.accountIds.contains(transaction.fromAccountId)
                val categoryMatches = budget.categories.isEmpty() ||
                    budget.categories.any {
                        it.equals(transaction.payee, true) ||
                            it.equals(transaction.description, true)
                    }

                if (typeMatches && accountMatches && categoryMatches) {
                    budgetSummaries.getOrPut(budget.id) { MutableBudgetSummary() }
                        .transactions.add(transaction)
                }
            }
        }

        val accountReports = accountSummaries.mapValues { (_, summary) ->
            AccountReportSummary(
                transactions = summary.transactions,
                income = summary.income,
                expense = summary.expense
            )
        }
        val budgetReports = budgetSummaries.mapValues { (_, summary) ->
            BudgetReportSummary(transactions = summary.transactions)
        }
        val categoryExpenses = categoryTotals.entries.sortedByDescending { it.value }

        ReportSummary(
            income = income,
            expense = expense,
            categoryExpenses = categoryExpenses,
            accountReports = accountReports,
            budgetReports = budgetReports
        )
    }

    val totalIncome = reportSummary.income
    val totalExpense = reportSummary.expense
    val netCashflow = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netCashflow / totalIncome) * 100).coerceIn(0.0, 100.0) else 0.0
    val categoryExpenses = reportSummary.categoryExpenses

    // Gemini AI Streaming advice generation callback
    fun generateAiAdvice() {
        val apiKey = customKey.ifBlank { defaultKey }
        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            showApiKeyDialog = true
            return
        }

        isLoadingAi = true
        cachedAiAdviceText = ""
        coroutineScope.launch {
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-3.5-flash-lite",
                    apiKey = apiKey
                )

                val topCats = categoryExpenses.take(5).joinToString(", ") { "${it.key}: $${String.format(currentLocale, "%.2f", it.value)}" }
                val accountsSummary = uiState.accounts.joinToString("; ") { "${it.name} (${it.type.name}): ${it.balance}" }
                val budgetsSummary = uiState.budgets.joinToString("; ") { "${it.name}: $${String.format(currentLocale, "%.2f", it.spent)} spent of $${String.format(currentLocale, "%.2f", it.amount)}" }

                val prompt = """
                    You are an expert personal finance advisor AI.
                    Analyze this comprehensive financial data for the period '${selectedPeriod.label}':

                    Cashflow Overview:
                    - Total Income: $${String.format(currentLocale, "%.2f", totalIncome)}
                    - Total Expenses: $${String.format(currentLocale, "%.2f", totalExpense)}
                    - Net Cashflow: $${String.format(currentLocale, "%.2f", netCashflow)}
                    - Savings Rate: ${String.format(currentLocale, "%.1f", savingsRate)}%
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

                var accumulatedText = ""
                generativeModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text
                    if (!chunkText.isNullOrEmpty()) {
                        accumulatedText += chunkText
                        cachedAiAdviceText = accumulatedText
                    }
                }

                if (accumulatedText.isEmpty()) {
                    cachedAiAdviceText = "No advice generated."
                }
            } catch (e: Exception) {
                cachedAiAdviceText = "Error generating AI advice: ${e.localizedMessage}"
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
                        generateAiAdvice()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector with keys for Recomposition optimization
            item(key = "period_selector") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(TimePeriod.entries, key = { it.name }) { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.label) }
                        )
                    }
                }
            }

            // Category Navigation Tabs (Overview, Accounts, Budgets, Charts)
            item(key = "navigation_tabs") {
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
                item(key = "overview_summary") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryMetricCard(
                            title = "Income",
                            amount = String.format(currentLocale, "$%.2f", totalIncome),
                            color = Color(0xFF4CAF50),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            title = "Expenses",
                            amount = String.format(currentLocale, "$%.2f", totalExpense),
                            color = Color(0xFFE53935),
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item(key = "overview_net_cashflow") {
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
                                    text = String.format(currentLocale, "$%.2f", netCashflow),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netCashflow >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Savings Rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = String.format(currentLocale, "%.1f%%", savingsRate),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // AI Financial Advisor Card (Streaming + Cached response)
                item(key = "ai_advice_card") {
                    AiAdviceCard(
                        aiAdviceText = cachedAiAdviceText,
                        isLoadingAi = isLoadingAi,
                        onGenerateAdvice = { generateAiAdvice() },
                        onOpenApiKeyDialog = { showApiKeyDialog = true },
                        onOpenAiChat = onOpenAiChat
                    )
                }
            }

            // TAB 1: ACCOUNTS ANALYTICS & SOURCE OF CHANGE
            if (selectedCategoryTab == 1) {
                item(key = "accounts_title") {
                    Text(
                        "Accounts Analytics & Source of Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.accounts, key = { "account_${it.id}" }) { account ->
                    AccountSourceAnalyticsCard(
                        account = account,
                        summary = reportSummary.accountReports[account.id]
                    )
                }
            }

            // TAB 2: BUDGETS BREAKDOWN & SOURCE OF CHANGE
            if (selectedCategoryTab == 2) {
                item(key = "budgets_title") {
                    Text(
                        "Budgets Performance & Transaction Sources",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.budgets.isEmpty()) {
                    item(key = "budgets_empty") {
                        EmptyChartPlaceholder("No budgets set up yet. Add budgets to see analytics!")
                    }
                } else {
                    items(uiState.budgets, key = { "budget_${it.id}" }) { budget ->
                        BudgetSourceAnalyticsCard(
                            budget = budget,
                            summary = reportSummary.budgetReports[budget.id],
                            allAccounts = uiState.accounts
                        )
                    }
                }
            }

            // TAB 3: CHARTS & TRENDS
            if (selectedCategoryTab == 3) {
                item(key = "charts_trends") {
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
    summary: AccountReportSummary?
) {
    val currentLocale = LocalLocale.current.platformLocale
    var expanded by remember { mutableStateOf(false) }
    val accountTransactions = summary?.transactions ?: emptyList()
    val accountIncome = summary?.income ?: 0.0
    val accountExpense = summary?.expense ?: 0.0

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
                    "Inflow: +$${String.format(currentLocale, "%.2f", accountIncome)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Outflow: -$${String.format(currentLocale, "%.2f", accountExpense)}",
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
                                text = (if (isPositive) "+" else "-") + String.format(currentLocale, "$%.2f", tx.amount),
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
    summary: BudgetReportSummary?,
    allAccounts: List<Account>
) {
    val currentLocale = LocalLocale.current.platformLocale
    var expanded by remember { mutableStateOf(false) }
    val budgetTransactions = summary?.transactions ?: emptyList()

    val progress = remember(budget) {
        if (budget.amount > 0) (budget.spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
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
                            .background(budget.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(budget.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format(currentLocale, "$%.2f / $%.2f", budget.spent, budget.amount),
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
                                text = String.format(currentLocale, "$%.2f", tx.amount),
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
    aiAdviceText: String?,
    isLoadingAi: Boolean,
    onGenerateAdvice: () -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    onOpenAiChat: () -> Unit = {}
) {
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
                IconButton(onClick = onOpenApiKeyDialog) {
                    Icon(Icons.Default.Key, contentDescription = "API Key", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (aiAdviceText == null && !isLoadingAi) {
                Text(
                    "Get comprehensive financial insights analyzing Accounts, Budgets, and Cashflow powered by Google Gemini AI (Streaming).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (isLoadingAi && aiAdviceText.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Connecting to Gemini AI Stream...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                if (!aiAdviceText.isNullOrEmpty()) {
                    Column {
                        if (isLoadingAi) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Streaming response...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(
                            text = aiAdviceText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGenerateAdvice,
                enabled = !isLoadingAi,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (aiAdviceText == null) "Get AI Analysis (Live Stream)" else "Refresh AI Analysis")
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
    val currentLocale = LocalLocale.current.platformLocale
    val monthsData = remember(transactions, currentLocale) {
        val sdf = SimpleDateFormat("MMM", currentLocale)
        val grouped = linkedMapOf<String, MutableMonthSummary>()

        transactions.forEach { transaction ->
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            val month = sdf.format(cal.time)
            val summary = grouped.getOrPut(month) { MutableMonthSummary() }
            if (transaction.type == TransactionType.INCOME) {
                summary.income += transaction.amount.toFloat()
            } else if (transaction.type == TransactionType.EXPENSE) {
                summary.expense += transaction.amount.toFloat()
            }
        }

        grouped.map { (month, summary) ->
            MonthSummary(month, summary.income, summary.expense)
        }.takeLast(6)
    }

    if (monthsData.isEmpty()) {
        EmptyChartPlaceholder("No transaction data available for this period")
        return
    }

    val maxAmount = remember(monthsData) {
        monthsData.flatMap { listOf(it.income, it.expense) }.maxOrNull()?.coerceAtLeast(100f) ?: 100f
    }

    // Pre-calculated & buffered Canvas graphics parameters to eliminate GC allocations during rendering
    val density = LocalDensity.current
    val barWidthPx = remember(density) { with(density) { 24.dp.toPx() } }
    val spacingPaddingPx = remember(density) { with(density) { 4.dp.toPx() } }
    val topPaddingPx = remember(density) { with(density) { 30.dp.toPx() } }
    val bottomPaddingPx = remember(density) { with(density) { 20.dp.toPx() } }
    val cornerRadius = remember(density) {
        val rx = with(density) { 8.dp.toPx() }
        CornerRadius(rx, rx)
    }
    val incomeColor = remember { Color(0xFF4CAF50) }
    val expenseColor = remember { Color(0xFFE53935) }

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
            val spacing = width / monthsData.size.coerceAtLeast(1)
            val maxBarHeight = height - topPaddingPx

            monthsData.forEachIndexed { index, data ->
                val xCenter = spacing * index + spacing / 2f
                val incomeBarHeight = (data.income / maxAmount) * maxBarHeight
                val expenseBarHeight = (data.expense / maxAmount) * maxBarHeight

                // Income Bar (Green)
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(xCenter - barWidthPx - spacingPaddingPx, height - incomeBarHeight - bottomPaddingPx),
                    size = Size(barWidthPx, incomeBarHeight),
                    cornerRadius = cornerRadius
                )

                // Expense Bar (Red)
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(xCenter + spacingPaddingPx, height - expenseBarHeight - bottomPaddingPx),
                    size = Size(barWidthPx, expenseBarHeight),
                    cornerRadius = cornerRadius
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
            LegendItem(incomeColor, "Income")
            LegendItem(expenseColor, "Expenses")
        }
    }
}

@Composable
fun CategoryDonutChart(categoryExpenses: List<Map.Entry<String, Float>>) {
    val currentLocale = LocalLocale.current.platformLocale
    if (categoryExpenses.isEmpty()) {
        EmptyChartPlaceholder("No expenses recorded in this period")
        return
    }

    val displayCategories = remember(categoryExpenses) {
        val topCategories = categoryExpenses.take(7)
        val remaining = categoryExpenses.drop(7)
        if (remaining.isEmpty()) {
            topCategories
        } else {
            val otherTotal = remaining.sumOf { it.value.toDouble() }.toFloat()
            topCategories + object : Map.Entry<String, Float> {
                override val key: String = "Other"
                override val value: Float = otherTotal
            }
        }
    }

    val totalExpense = remember(displayCategories) {
        displayCategories.sumOf { it.value.toDouble() }.toFloat()
    }
    
    val density = LocalDensity.current
    val stroke = remember(density) { Stroke(width = with(density) { 28.dp.toPx() }) }
    val sliceColors = remember {
        listOf(
            Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF4CAF50), Color(0xFFFB8C00),
            Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFFFFB300), Color(0xFFD81B60)
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Expense Breakdown by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                displayCategories.forEachIndexed { index, entry ->
                    val sweepAngle = if (totalExpense > 0f) (entry.value / totalExpense) * 360f else 0f
                    val color = sliceColors[index % sliceColors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = stroke
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = String.format(currentLocale, "$%.0f", totalExpense),
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
                        text = String.format(currentLocale, "$%.2f (%.1f%%)", entry.value, percent),
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
        val cumulativePoints = mutableListOf<Float>()

        sorted.forEach { tx ->
            if (tx.type == TransactionType.INCOME) runningBalance += tx.amount.toFloat()
            if (tx.type == TransactionType.EXPENSE) runningBalance -= tx.amount.toFloat()
            cumulativePoints.add(runningBalance)
        }

        if (cumulativePoints.size <= 120) {
            cumulativePoints
        } else {
            val step = (cumulativePoints.size - 1).toFloat() / 119f
            buildList {
                for (index in 0 until 120) {
                    val sourceIndex = (index * step).toInt().coerceIn(0, cumulativePoints.lastIndex)
                    add(cumulativePoints[sourceIndex])
                }
            }.distinct()
        }
    }

    if (trendData.isEmpty()) {
        EmptyChartPlaceholder("No trend data available for this period")
        return
    }

    val minVal = remember(trendData) { trendData.minOrNull() ?: 0f }
    val maxVal = remember(trendData, minVal) { trendData.maxOrNull()?.coerceAtLeast(minVal + 1f) ?: 100f }
    
    // Reusable Path and Graphics objects to prevent GC frame drops
    val path = remember { Path() }
    val fillPath = remember { Path() }
    val density = LocalDensity.current
    val stroke = remember(density) { Stroke(width = with(density) { 3.dp.toPx() }) }
    val pointRadius = remember(density) { with(density) { 4.dp.toPx() } }
    val topPaddingPx = remember(density) { with(density) { 30.dp.toPx() } }
    val bottomPaddingPx = remember(density) { with(density) { 15.dp.toPx() } }
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientBrush = remember(primaryColor) {
        Brush.verticalGradient(
            colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Cumulative Balance Growth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            path.reset()
            fillPath.reset()

            val width = size.width
            val height = size.height
            val spacing = width / (trendData.size - 1).coerceAtLeast(1)
            val usableHeight = height - topPaddingPx - bottomPaddingPx

            trendData.forEachIndexed { index, value ->
                val x = index * spacing
                val normalizedY = (value - minVal) / (maxVal - minVal)
                val y = height - (normalizedY * usableHeight) - bottomPaddingPx

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
                drawCircle(color = primaryColor, radius = pointRadius, center = Offset(x, y))
            }

            // Draw area gradient fill
            drawPath(
                path = fillPath,
                brush = gradientBrush
            )

            // Draw line
            drawPath(
                path = path,
                color = primaryColor,
                style = stroke
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

@Immutable
data class MonthSummary(val month: String, val income: Float, val expense: Float)
