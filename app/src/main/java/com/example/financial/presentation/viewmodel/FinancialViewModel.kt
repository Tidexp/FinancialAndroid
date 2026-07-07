package com.example.financial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financial.FinancialApplication
import com.example.financial.data.local.PreferenceManager
import com.example.financial.data.repository.AuthRepository
import com.example.financial.data.repository.FinancialRepository
import com.example.financial.data.repository.SyncRepository
import com.example.financial.domain.model.*
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
    val dbStatus: String = "Checking...",
    val syncStatus: String = "Up to date",
    val message: String? = null
)

data class StatisticsUiState(
    val categories: List<CategorySpending> = emptyList(),
    val graphData: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.4f),
    val isLoading: Boolean = false
)

data class ReportsUiState(
    val data: ReportsData? = null,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

class FinancialViewModel(
    private val repository: FinancialRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialApplication)
                val database = application.database
                val preferenceManager = PreferenceManager(application)
                val firestore = FirebaseFirestore.getInstance()
                val authRepo = AuthRepository(firestore, database, preferenceManager)
                val repository = FinancialRepository(
                    database.transactionDao(),
                    database.accountDao(),
                    database.accountGroupDao(),
                    database.budgetDao(),
                    firestore,
                    authRepo
                )
                val syncRepo = SyncRepository(
                    database.transactionDao(),
                    database.accountDao(),
                    database.accountGroupDao(),
                    database.budgetDao(),
                    firestore,
                    authRepo
                )
                FinancialViewModel(repository, authRepo, syncRepo)
            }
        }
    }

    private val _homeUiState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _statsUiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val statsUiState: StateFlow<StatisticsUiState> = _statsUiState.asStateFlow()

    private val _reportsUiState = MutableStateFlow(ReportsUiState(
        startDate = java.util.Calendar.getInstance().apply { 
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
        }.timeInMillis,
        endDate = System.currentTimeMillis(),
        isLoading = true
    ))
    val reportsUiState: StateFlow<ReportsUiState> = _reportsUiState.asStateFlow()

    init {
        checkConnections()
        loadHomeData()
        loadStatsData()
        loadReportsData()
    }

    fun signInAnonymously(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _homeUiState.update { it.copy(isLoading = true, message = null) }
            authRepository.signInAnonymously().onSuccess {
                _homeUiState.update { it.copy(isLoading = false, authStatus = "Guest Mode", message = "Đã vào chế độ khách") }
                onSuccess()
            }.onFailure { e ->
                _homeUiState.update { it.copy(isLoading = false, message = "Lỗi: ${e.message}") }
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _homeUiState.update { it.copy(isLoading = true, message = null) }
            authRepository.signUp(email, password).onSuccess {
                _homeUiState.update { it.copy(isLoading = false, authStatus = email, message = "Đăng ký thành công!") }
                // Pull data after login
                syncRepository.syncDown()
                onSuccess()
            }.onFailure { e ->
                _homeUiState.update { it.copy(isLoading = false, message = "Đăng ký thất bại: ${e.message}") }
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _homeUiState.update { it.copy(isLoading = true, message = null) }
            authRepository.signIn(email, password).onSuccess {
                _homeUiState.update { it.copy(isLoading = false, authStatus = email, message = "Đăng nhập thành công!") }
                // Pull data after login
                syncRepository.syncDown()
                onSuccess()
            }.onFailure { e ->
                _homeUiState.update { it.copy(isLoading = false, message = "Đăng nhập thất bại: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _homeUiState.update { it.copy(message = null) }
    }

    fun signOut(onComplete: () -> Unit) {
        authRepository.signOut()
        _homeUiState.update { it.copy(authStatus = "Not Logged In") }
        onComplete()
    }

    private fun checkConnections() {
        viewModelScope.launch {
            authRepository.currentUser?.let { user ->
                _homeUiState.update { it.copy(authStatus = "Logged in as ${user.username}") }
                // Initial sync up on start if logged in
                syncRepository.syncUp()
            }
            repository.getAccounts().take(1).collect {
                _homeUiState.update { it.copy(dbStatus = "Local DB Ready") }
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
            syncRepository.syncUp()
        }
    }

    fun deleteAccountGroup(group: AccountGroup) {
        viewModelScope.launch {
            repository.deleteAccountGroup(group)
            syncRepository.syncUp()
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
            syncRepository.syncUp()
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
            syncRepository.syncUp()
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            syncRepository.syncUp()
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
            syncRepository.syncUp()
        }
    }

    fun updateAccountGroup(group: AccountGroup) {
        viewModelScope.launch {
            repository.updateAccountGroup(group)
            syncRepository.syncUp()
        }
    }

    fun updateAccountOrder(accounts: List<Account>) {
        viewModelScope.launch {
            accounts.forEachIndexed { index, account ->
                repository.updateAccount(account.copy(orderIndex = index))
            }
            syncRepository.syncUp()
        }
    }

    fun updateGroupOrder(groups: List<AccountGroup>) {
        viewModelScope.launch {
            groups.forEachIndexed { index, group ->
                repository.updateAccountGroup(group.copy(orderIndex = index))
            }
            syncRepository.syncUp()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)

            // If it was CLEARED, we might need to reverse the balance update.
            if (transaction.status != TransactionStatus.CLEARED) {
                syncRepository.syncUp()
                return@launch
            }

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
                else -> {}
            }
            syncRepository.syncUp()
        }
    }

    fun payScheduledTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val payment = transaction.copy(
                id = java.util.UUID.randomUUID().toString(),
                status = TransactionStatus.CLEARED,
                date = System.currentTimeMillis()
            )
            addTransaction(payment)
            rescheduleTransaction(transaction)
            syncRepository.syncUp()
        }
    }

    fun skipScheduledTransaction(transaction: Transaction) {
        viewModelScope.launch {
            rescheduleTransaction(transaction)
            syncRepository.syncUp()
        }
    }

    private suspend fun rescheduleTransaction(transaction: Transaction) {
        val recurrence = transaction.recurrence
        if (recurrence == null) {
            repository.deleteTransaction(transaction)
            return
        }

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
            _homeUiState.update { it.copy(syncStatus = "Syncing...") }
            repository.addTransaction(transaction)
            
            if (transaction.fromAccountId.startsWith("budget_")) {
                syncRepository.syncUp()
                _homeUiState.update { it.copy(syncStatus = "Synced") }
                return@launch
            }

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
                        newBalance = transaction.amount
                    }
                    TransactionType.TRANSFER -> {
                        newBalance -= transaction.amount
                        transaction.toAccountId?.let { toId ->
                            currentAccounts.find { it.id == toId }?.let { toAcc ->
                                val toNewBal = parseBalance(toAcc.balance) + transaction.amount
                                repository.updateAccount(toAcc.copy(balance = formatBalance(toNewBal)))
                            }
                        }
                    }
                    TransactionType.EXCHANGE -> {
                        newBalance -= (transaction.amount + (transaction.commission ?: 0.0))
                    }
                }
                repository.updateAccount(account.copy(balance = formatBalance(newBalance)))
            }
            syncRepository.syncUp()
            _homeUiState.update { it.copy(syncStatus = "Synced") }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { 
            repository.deleteBudget(budget)
            syncRepository.syncUp()
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch { 
            repository.updateBudget(budget)
            syncRepository.syncUp()
        }
    }

    fun transferBudget(fromId: String, toId: String, amount: Double, memo: String, date: Long) {
        viewModelScope.launch {
            val budgets = _homeUiState.value.budgets
            val fromB = budgets.find { it.id == fromId }
            val toB = budgets.find { it.id == toId }
            if (fromB != null && toB != null) {
                repository.updateBudget(fromB.copy(amount = fromB.amount - amount))
                repository.updateBudget(toB.copy(amount = toB.amount + amount))

                addTransaction(Transaction(
                    id = java.util.UUID.randomUUID().toString(),
                    type = TransactionType.TRANSFER,
                    fromAccountId = "budget_internal",
                    amount = amount,
                    description = "Budget Transfer: ${fromB.name} -> ${toB.name}",
                    memo = memo,
                    date = date,
                    budgetId = fromId
                ))
            }
            syncRepository.syncUp()
        }
    }
    fun addCreditAccount(name: String, balance: String, limit: String, icon: String?, day: String, auto: Boolean, info: String, gId: String?, mId: String? = null) {
        viewModelScope.launch { 
            repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = balance, type = AccountType.CREDIT, color = androidx.compose.ui.graphics.Color(0xFFE91E63), iconUri = icon, creditLimit = limit, statementCloseDay = day, autoClear = auto, additionalInfo = info, groupId = gId, monitoredByBudgetId = mId))
            syncRepository.syncUp()
        }
    }

    fun addLoanAccount(name: String, princ: String, apr: String, dur: String, start: String, first: String, gId: String?, info: String, mId: String? = null) {
        viewModelScope.launch { 
            repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = "-$princ", type = AccountType.LOAN, color = androidx.compose.ui.graphics.Color(0xFF4CAF50), principalAmount = princ, apr = apr, duration = dur, startDate = start, firstDueDate = first, groupId = gId, additionalInfo = info, monitoredByBudgetId = mId))
            syncRepository.syncUp()
        }
    }

    fun addInvestmentAccount(name: String, bal: String, date: String, gId: String?, info: String, mId: String? = null) {
        viewModelScope.launch { 
            repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = bal, type = AccountType.INVESTMENT, color = androidx.compose.ui.graphics.Color(0xFF2196F3), asOfDate = date, groupId = gId, additionalInfo = info, monitoredByBudgetId = mId))
            syncRepository.syncUp()
        }
    }

    fun addForexAccount(name: String, curr: String, gId: String?, info: String, mId: String? = null) {
        viewModelScope.launch { 
            repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = "0.00", type = AccountType.FOREX, color = androidx.compose.ui.graphics.Color(0xFFFF9800), currency = curr, groupId = gId, additionalInfo = info, monitoredByBudgetId = mId))
            syncRepository.syncUp()
        }
    }

    fun addStandardAccount(name: String, bal: String, type: AccountType, gId: String?, auto: Boolean, info: String, mId: String? = null) {
        viewModelScope.launch { 
            repository.addAccount(Account(id = java.util.UUID.randomUUID().toString(), name = name, balance = bal, type = type, color = androidx.compose.ui.graphics.Color(0xFF9C27B0), groupId = gId, autoClear = auto, additionalInfo = info, monitoredByBudgetId = mId))
            syncRepository.syncUp()
        }
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

    private fun formatBalance(balance: Double): String = String.format(java.util.Locale.getDefault(), "$%.2f", balance)

    @OptIn(FlowPreview::class)
    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getBalanceData(),
                repository.getAccounts(),
                repository.getAccountGroups(),
                repository.getBudgets(),
                repository.getBudgetGroups(),
                repository.getTransactions()
            ) { args ->
                stateUpdate(args[0] as BalanceData, args[1] as List<Account>, args[2] as List<AccountGroup>, args[3] as List<Budget>, args[4] as List<BudgetGroup>, args[5] as List<Transaction>)
            }.debounce(100).flowOn(Dispatchers.Default).collect { update ->
                _homeUiState.update { update(it) }
            }
        }
    }

    private fun stateUpdate(balance: BalanceData, accounts: List<Account>, accountGroups: List<AccountGroup>, budgets: List<Budget>, budgetGroups: List<BudgetGroup>, transactions: List<Transaction>): (HomeUiState) -> HomeUiState = { currentState ->
        val expenseBudgets = budgets.filter { !it.isIncome }
        val totalBudgeted = expenseBudgets.sumOf { it.amount }
        val totalSpent = expenseBudgets.sumOf { it.spent }
        currentState.copy(
            balanceData = balance,
            accounts = accounts,
            accountGroups = accountGroups,
            budgets = budgets,
            budgetGroups = budgetGroups,
            transactions = transactions,
            totalBudgeted = formatBalance(totalBudgeted),
            remainingBudget = formatBalance(totalBudgeted - totalSpent),
            isLoading = false
        )
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            repository.getCategorySpending().flowOn(Dispatchers.IO).collect { categories ->
                _statsUiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }

    private var reportsJob: kotlinx.coroutines.Job? = null
    private fun loadReportsData() {
        reportsJob?.cancel()
        reportsJob = viewModelScope.launch {
            _reportsUiState
                .map { it.startDate to it.endDate }
                .distinctUntilChanged()
                .collectLatest { (start, end) ->
                    repository.getReportsData(start, end)
                        .collect { data ->
                            _reportsUiState.update { it.copy(data = data, isLoading = false) }
                        }
                }
        }
    }

    fun setReportsDateRange(start: Long, end: Long) {
        _reportsUiState.update { it.copy(startDate = start, endDate = end, isLoading = true) }
    }
}
