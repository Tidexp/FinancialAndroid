package com.example.financial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financial.FinancialApplication
import com.example.financial.data.local.UserPreferences
import com.example.financial.data.repository.AuthRepository
import com.example.financial.data.repository.FinancialRepository
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val balanceData: BalanceData? = null,
    val accounts: List<Account> = emptyList(),
    val accountGroups: List<AccountGroup> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val budgetGroups: List<BudgetGroup> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val remainingBudget: String = "$0.00",
    val totalBudgeted: String = "$0.00",
    val isLoading: Boolean = false,
    val authStatus: String = "Connecting...",
    val dbStatus: String = "Checking..."
)

data class StatisticsUiState(
    val categories: List<CategorySpending> = emptyList(),
    val graphData: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.4f),
    val isLoading: Boolean = false
)

class FinancialViewModel(
    private val repository: FinancialRepository,
    private val authRepository: AuthRepository,
    val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialApplication)
                val database = application.database
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val userPrefs = UserPreferences(application)
                val repository = FinancialRepository(
                    database.transactionDao(),
                    database.accountDao(),
                    database.accountGroupDao(),
                    database.budgetDao(),
                    firestore,
                    auth
                )
                FinancialViewModel(repository, AuthRepository(auth), userPrefs)
            }
        }
    }

    private val _homeUiState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _statsUiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val statsUiState: StateFlow<StatisticsUiState> = _statsUiState.asStateFlow()

    init {
        checkConnections()
        loadHomeData()
        loadStatsData()
        observeAuthState()
        observeCurrency()
    }

    private fun observeCurrency() {
        viewModelScope.launch {
            userPreferences.currency.collect {
                loadHomeData()
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                if (user != null) {
                    if (user.isAnonymous) {
                        _homeUiState.update { it.copy(authStatus = "Guest Mode", dbStatus = "Local DB Ready") }
                    } else {
                        val name = user.displayName ?: user.email?.substringBefore("@") ?: user.uid.take(6)
                        _homeUiState.update { it.copy(authStatus = "Google ($name)", dbStatus = "Local DB Ready") }
                        repository.syncFromCloud()
                        repository.listenToCloudSync(viewModelScope)
                        _homeUiState.update { it.copy(dbStatus = "Cloud Synced") }
                    }
                } else {
                    _homeUiState.update { it.copy(authStatus = "Not Authenticated", dbStatus = "Local DB Ready") }
                }
            }
        }
    }

    private fun checkConnections() {
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                if (user.isAnonymous) {
                    _homeUiState.update { it.copy(authStatus = "Guest Mode", dbStatus = "Local DB Ready") }
                } else {
                    val name = user.displayName ?: user.email?.substringBefore("@") ?: user.uid.take(6)
                    _homeUiState.update { it.copy(authStatus = "Google ($name)", dbStatus = "Local DB Ready") }
                }
            } else {
                _homeUiState.update { it.copy(authStatus = "Not Authenticated", dbStatus = "Local DB Ready") }
            }
        }
    }

    fun addAccountGroup(
        name: String,
        iconName: String?,
        iconUri: String?,
        color: androidx.compose.ui.graphics.Color,
        accountIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val groupId = java.util.UUID.randomUUID().toString()
            val group = AccountGroup(
                id = groupId,
                name = name,
                iconName = iconName,
                iconUri = iconUri,
                color = color
            )
            repository.addAccountGroup(group)

            // Update selected accounts with the new groupId
            if (accountIds.isNotEmpty()) {
                val currentAccounts = _homeUiState.value.accounts
                accountIds.forEach { id ->
                    currentAccounts.find { it.id == id }?.let { account ->
                        repository.updateAccount(account.copy(groupId = groupId))
                    }
                }
            }
        }
    }

    fun deleteAccountGroup(group: AccountGroup) {
        viewModelScope.launch {
            // Optional: Handle accounts belonging to this group (maybe set groupId to null)
            repository.deleteAccountGroup(group)
        }
    }

    fun addBudget(
        name: String,
        amount: Double,
        isIncome: Boolean,
        color: androidx.compose.ui.graphics.Color,
        groupId: String? = null,
        startDate: Long = System.currentTimeMillis(),
        repeatEnabled: Boolean = true,
        frequencyValue: Int = 1,
        frequencyUnit: String = "month",
        rolloverEnabled: Boolean = false,
        accountIds: List<String> = emptyList(),
        categories: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val budget = Budget(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                amount = amount,
                isIncome = isIncome,
                color = color,
                budgetGroupId = groupId,
                startDate = startDate,
                repeatEnabled = repeatEnabled,
                frequencyValue = frequencyValue,
                frequencyUnit = frequencyUnit,
                rolloverEnabled = rolloverEnabled,
                accountIds = accountIds,
                categories = categories
            )
            repository.addBudget(budget)
        }
    }

    fun addBudgetGroup(name: String, color: androidx.compose.ui.graphics.Color) {
        viewModelScope.launch {
            val group = BudgetGroup(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                color = color
            )
            repository.addBudgetGroup(group)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun updateAccountGroup(group: AccountGroup) {
        viewModelScope.launch {
            repository.updateAccountGroup(group)
        }
    }

    fun updateAccountOrder(accounts: List<Account>) {
        viewModelScope.launch {
            accounts.forEachIndexed { index, account ->
                repository.updateAccount(account.copy(orderIndex = index))
            }
        }
    }

    fun updateGroupOrder(groups: List<AccountGroup>) {
        viewModelScope.launch {
            groups.forEachIndexed { index, group ->
                repository.updateAccountGroup(group.copy(orderIndex = index))
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id.startsWith("loan_payment_")) {
                val accountId = transaction.id.removePrefix("loan_payment_")
                _homeUiState.value.accounts.find { it.id == accountId }?.let { account ->
                    repository.updateAccount(account.copy(firstDueDate = null))
                }
                return@launch
            }
            repository.deleteTransaction(transaction)

            // If it was CLEARED, we might need to reverse the balance update.
            // But for PLANNED (scheduled), we just delete it.
            if (transaction.status != TransactionStatus.CLEARED) return@launch

            val currentAccounts = _homeUiState.value.accounts
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { account ->
                        val newBalance = parseBalance(account.balance) + transaction.amount
                        repository.updateAccount(account.copy(balance = formatBalance(newBalance)))
                    }
                }
                TransactionType.INCOME -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { account ->
                        val newBalance = parseBalance(account.balance) - transaction.amount
                        repository.updateAccount(account.copy(balance = formatBalance(newBalance)))
                    }
                }
                TransactionType.TRANSFER -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { fromAcc ->
                        val newFromBalance = parseBalance(fromAcc.balance) + transaction.amount
                        repository.updateAccount(fromAcc.copy(balance = formatBalance(newFromBalance)))
                    }
                    transaction.toAccountId?.let { toId ->
                        currentAccounts.find { it.id == toId }?.let { toAcc ->
                            val newToBalance = parseBalance(toAcc.balance) - transaction.amount
                            repository.updateAccount(toAcc.copy(balance = formatBalance(newToBalance)))
                        }
                    }
                }
                TransactionType.ADJUSTMENT -> {
                    // This is tricky because we don't know the previous balance
                    // Maybe we shouldn't reverse adjustment directly without more info
                }
                else -> {}
            }
        }
    }

    fun payScheduledTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id.startsWith("loan_payment_")) {
                val accountId = transaction.id.removePrefix("loan_payment_")
                val currentAccounts = _homeUiState.value.accounts
                currentAccounts.find { it.id == accountId }?.let { account ->
                    val fromId = account.paymentAccountId ?: account.id
                    val amount = Math.abs(transaction.amount)
                    
                    // 1. Create a CLEARED transaction (the payment itself)
                    val isTransfer = account.paymentAccountId != null
                    val payment = transaction.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        type = if (isTransfer) TransactionType.TRANSFER else TransactionType.INCOME,
                        fromAccountId = fromId,
                        toAccountId = if (isTransfer) account.id else null,
                        amount = amount,
                        status = TransactionStatus.CLEARED,
                        date = System.currentTimeMillis()
                    )
                    
                    // 2. Add transaction and update balance
                    addTransactionSync(payment)

                    // 3. Advance due date
                    advanceLoanDueDate(accountId)
                }
                return@launch
            }
            // 1. Create a CLEARED transaction (the payment itself)
            val payment = transaction.copy(
                id = java.util.UUID.randomUUID().toString(),
                status = TransactionStatus.CLEARED,
                date = System.currentTimeMillis()
            )
            addTransactionSync(payment)

            // 2. Reschedule the original one
            rescheduleTransaction(transaction)
        }
    }

    fun skipScheduledTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id.startsWith("loan_payment_")) {
                val accountId = transaction.id.removePrefix("loan_payment_")
                advanceLoanDueDate(accountId)
                return@launch
            }
            rescheduleTransaction(transaction)
        }
    }

    private suspend fun advanceLoanDueDate(accountId: String) {
        repository.getAccounts().take(1).collect { accounts ->
            accounts.find { it.id == accountId }?.let { account ->
                try {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                    val nextDueDate = if (!account.firstDueDate.isNullOrBlank()) {
                        val date = sdf.parse(account.firstDueDate)
                        if (date != null) {
                            val calendar = java.util.Calendar.getInstance()
                            calendar.time = date
                            calendar.add(java.util.Calendar.MONTH, 1)
                            sdf.format(calendar.time)
                        } else null
                    } else null

                    repository.updateAccount(account.copy(
                        firstDueDate = nextDueDate,
                        paymentsMade = account.paymentsMade + 1
                    ))
                } catch (e: Exception) {}
            }
        }
    }

    private suspend fun rescheduleTransaction(transaction: Transaction) {
        val recurrence = transaction.recurrence
        if (recurrence == null) {
            // If no recurrence, this was a one-time scheduled transaction
            repository.deleteTransaction(transaction)
            return
        }

        // Calculate next date
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = transaction.date

        when (recurrence.frequencyUnit.lowercase()) {
            "day" -> calendar.add(java.util.Calendar.DAY_OF_YEAR, recurrence.frequencyValue)
            "week" -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, recurrence.frequencyValue)
            "month" -> calendar.add(java.util.Calendar.MONTH, recurrence.frequencyValue)
            "year" -> calendar.add(java.util.Calendar.YEAR, recurrence.frequencyValue)
            else -> calendar.add(java.util.Calendar.MONTH, recurrence.frequencyValue)
        }

        val nextTransaction = transaction.copy(date = calendar.timeInMillis)

        // Update or Delete based on end criteria
        if (recurrence.endType == "After") {
            val remainingCount = recurrence.endAfterCount - 1
            if (remainingCount <= 0) {
                repository.deleteTransaction(transaction)
            } else {
                repository.addTransaction(nextTransaction.copy(
                    recurrence = recurrence.copy(endAfterCount = remainingCount)
                ))
            }
        } else {
            repository.addTransaction(nextTransaction)
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            addTransactionSync(transaction)
        }
    }

    private suspend fun addTransactionSync(transaction: Transaction) {
        repository.addTransaction(transaction)
        
        // Chỉ cập nhật số dư nếu KHÔNG PHẢI giao dịch ảo của Budget
        if (transaction.fromAccountId.startsWith("budget_")) return

        val currentAccounts = _homeUiState.value.accounts
        val account = currentAccounts.find { it.id == transaction.fromAccountId }

        if (account != null) {
            var newBalance = parseBalance(account.balance)

            when (transaction.type) {
                TransactionType.EXPENSE, TransactionType.BUY -> {
                    newBalance -= transaction.amount
                }
                TransactionType.INCOME, TransactionType.SELL -> {
                    newBalance += transaction.amount
                }
                TransactionType.ADJUSTMENT -> {
                    // Trong AdjustBalanceScreen, amount được gửi là giá trị số dư MỚI
                    newBalance = transaction.amount
                }
                TransactionType.TRANSFER -> {
                    newBalance -= transaction.amount
                    // Cập nhật tài khoản nhận
                    transaction.toAccountId?.let { toId ->
                        currentAccounts.find { it.id == toId }?.let { toAcc ->
                            val toNewBal = parseBalance(toAcc.balance) + transaction.amount
                            repository.updateAccount(toAcc.copy(balance = formatBalance(toNewBal)))
                        }
                    }
                }
                TransactionType.EXCHANGE -> {
                    // Trừ tiền ở account nguồn + phí hoa hồng (nếu có)
                    newBalance -= (transaction.amount + (transaction.commission ?: 0.0))
                }
            }
            repository.updateAccount(account.copy(balance = formatBalance(newBalance)))
        }
    }

    // --- BUDGET METHODS (PHẦN LÀM MỚI TÁCH BIỆT) ---

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { repository.deleteBudget(budget) }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch { repository.updateBudget(budget) }
    }

    fun transferBudget(fromId: String, toId: String, amount: Double, memo: String, date: Long) {
        viewModelScope.launch {
            val budgets = _homeUiState.value.budgets
            val fromB = budgets.find { it.id == fromId }
            val toB = budgets.find { it.id == toId }
            if (fromB != null && toB != null) {
                repository.updateBudget(fromB.copy(amount = fromB.amount - amount))
                repository.updateBudget(toB.copy(amount = toB.amount + amount))

                // Lưu lịch sử CHỈ cho budget
                addTransaction(Transaction(
                    id = java.util.UUID.randomUUID().toString(),
                    type = TransactionType.TRANSFER,
                    fromAccountId = "budget_internal",
                    amount = amount,
                    description = "Budget Transfer: ${fromB.name} -> ${toB.name}",
                    memo = memo,
                    date = date,
                    budgetId = fromId // Gắn vào budget nguồn để xem lịch sử
                ))
            }
        }
    }
    fun addCreditAccount(name: String, balance: String, limit: String, icon: String?, day: String, auto: Boolean, info: String, gId: String?, mId: String? = null) {
        viewModelScope.launch { repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = balance, type = AccountType.CREDIT, color = androidx.compose.ui.graphics.Color(0xFFE91E63), iconUri = icon, creditLimit = limit, statementCloseDay = day, autoClear = auto, additionalInfo = info, groupId = gId, monitoredByBudgetId = mId)) }
    }

    fun addLoanAccount(name: String, princ: String, apr: String, dur: String, start: String, first: String, gId: String?, info: String, mId: String? = null, pAccId: String? = null, pCat: String? = null, pPayee: String? = null) {
        viewModelScope.launch { repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = "-$princ", type = AccountType.LOAN, color = androidx.compose.ui.graphics.Color(0xFF4CAF50), principalAmount = princ, apr = apr, duration = dur, startDate = start, firstDueDate = first, groupId = gId, additionalInfo = info, monitoredByBudgetId = mId, paymentAccountId = pAccId, paymentCategory = pCat, paymentPayee = pPayee)) }
    }

    fun addInvestmentAccount(name: String, bal: String, date: String, gId: String?, info: String, mId: String? = null) {
        viewModelScope.launch { repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = bal, type = AccountType.INVESTMENT, color = androidx.compose.ui.graphics.Color(0xFF2196F3), asOfDate = date, groupId = gId, additionalInfo = info, monitoredByBudgetId = mId)) }
    }

    fun addForexAccount(name: String, curr: String, gId: String?, info: String, mId: String? = null) {
        viewModelScope.launch { repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = "0.00", type = AccountType.FOREX, color = androidx.compose.ui.graphics.Color(0xFFFF9800), currency = curr, groupId = gId, additionalInfo = info, monitoredByBudgetId = mId)) }
    }

    fun addStandardAccount(name: String, bal: String, type: AccountType, gId: String?, auto: Boolean, info: String, mId: String? = null) {
        viewModelScope.launch { repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = bal, type = type, color = androidx.compose.ui.graphics.Color(0xFF9C27B0), groupId = gId, autoClear = auto, additionalInfo = info, monitoredByBudgetId = mId)) }
    }

    private fun parseBalance(balance: String): Double {
        return try {
            val normalized = balance.replace(",", ".")
            val clean = normalized.replace(Regex("[^0-9.-]"), "")
            val lastDotIndex = clean.lastIndexOf('.')
            if (lastDotIndex != -1) {
                val integerPart = clean.substring(0, lastDotIndex).replace(".", "")
                val fractionalPart = clean.substring(lastDotIndex + 1)
                (integerPart + "." + fractionalPart).toDouble()
            } else {
                clean.toDouble()
            }
        } catch (e: Exception) { 0.0 }
    }

    private fun formatBalance(balance: Double): String = userPreferences.formatAmount(balance)

    @OptIn(FlowPreview::class)
    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getAccounts(),
                repository.getAccountGroups(),
                repository.getRawBudgets(),
                repository.getBudgetGroups(),
                repository.getTransactions()
            ) { accounts, accountGroups, rawBudgets, budgetGroups, transactions ->
                val realTransactions = transactions.filter { it.budgetId == null }
                var total = 0.0
                var liab = 0.0
                accounts.forEach { a ->
                    val b = parseBalance(a.balance)
                    if (b < 0) liab += kotlin.math.abs(b)
                    total += b
                }
                val inc = realTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val exp = realTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val balanceData = BalanceData(
                    netWorth = formatBalance(total),
                    liabilities = formatBalance(-liab),
                    totalIncome = formatBalance(inc),
                    totalExpenses = formatBalance(exp),
                    monthlyBudget = 0f
                )

                val now = System.currentTimeMillis()
                val processedBudgets = rawBudgets.map { budget ->
                    val relevantTransactions = transactions.filter { t ->
                        if (t.budgetId == budget.id) return@filter true
                        if (t.budgetId != null) return@filter false
                        val typeMatch = t.type == (if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE)
                        val accMatch = budget.accountIds.isEmpty() || budget.accountIds.contains(t.fromAccountId)
                        val catMatch = budget.categories.isEmpty() || budget.categories.any { it.equals(t.payee, true) || it.equals(t.description, true) }
                        typeMatch && accMatch && catMatch
                    }

                    val periodMillis: Long = when (budget.frequencyUnit.lowercase()) {
                        "day" -> 24L * 3600000; "week" -> 7L * 24 * 3600000; "year" -> 365L * 24 * 3600000; else -> 30L * 24 * 3600000
                    }
                    val timePassed = now - budget.startDate
                    val periodsPassed = if (timePassed > 0) (timePassed / periodMillis).toInt() else 0
                    val currentPeriodStart = budget.startDate + (periodsPassed * periodMillis)

                    val spentInCurrentPeriod = relevantTransactions.filter { it.date >= currentPeriodStart }.sumOf { it.amount }

                    var rollover = 0.0
                    if (budget.rolloverEnabled && periodsPassed > 0) {
                        val pastSpent = relevantTransactions.filter { it.date >= budget.startDate && it.date < currentPeriodStart }.sumOf { it.amount }
                        val pastBudgeted = budget.amount * periodsPassed
                        rollover = if (budget.isIncome) pastSpent - pastBudgeted else pastBudgeted - pastSpent
                    }

                    budget.copy(
                        spent = spentInCurrentPeriod,
                        remaining = budget.amount - spentInCurrentPeriod + rollover,
                        progress = if (budget.amount > 0) (spentInCurrentPeriod / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
                    )
                }

                val expenseBudgets = processedBudgets.filter { !it.isIncome }
                val totalBudgeted = expenseBudgets.sumOf { it.amount }
                val totalSpent = expenseBudgets.sumOf { it.spent }

                listOf(
                    balanceData,
                    accounts,
                    accountGroups,
                    processedBudgets,
                    budgetGroups,
                    transactions,
                    formatBalance(totalBudgeted),
                    formatBalance(totalBudgeted - totalSpent)
                )
            }.debounce(50).flowOn(Dispatchers.Default).collect { data ->
                @Suppress("UNCHECKED_CAST")
                _homeUiState.update { currentState ->
                    currentState.copy(
                        balanceData = data[0] as BalanceData,
                        accounts = data[1] as List<Account>,
                        accountGroups = data[2] as List<AccountGroup>,
                        budgets = data[3] as List<Budget>,
                        budgetGroups = data[4] as List<BudgetGroup>,
                        transactions = data[5] as List<Transaction>,
                        totalBudgeted = data[6] as String,
                        remainingBudget = data[7] as String,
                        dbStatus = if (currentState.dbStatus == "Checking...") "Local DB Ready" else currentState.dbStatus,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            repository.getCategorySpending().flowOn(Dispatchers.IO).collect { categories ->
                _statsUiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }
}
