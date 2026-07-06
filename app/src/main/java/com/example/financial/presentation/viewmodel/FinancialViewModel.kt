package com.example.financial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financial.FinancialApplication
import com.example.financial.data.repository.AuthRepository
import com.example.financial.data.repository.FinancialRepository
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
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
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialApplication)
                val database = application.database
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val repository = FinancialRepository(
                    database.transactionDao(),
                    database.accountDao(),
                    database.accountGroupDao(),
                    database.budgetDao(),
                    firestore,
                    auth
                )
                FinancialViewModel(repository, AuthRepository(auth))
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
    }

    private fun checkConnections() {
        viewModelScope.launch {
            // Check Firebase Auth
            if (authRepository.currentUser != null) {
                _homeUiState.update {
                    it.copy(
                        authStatus = "Connected (${
                            authRepository.currentUser?.uid?.take(
                                6
                            )
                        })"
                    )
                }
            } else {
                val result = authRepository.signInAnonymously()
                result.onSuccess { user ->
                    val status =
                        if (user != null) "Connected (${user.uid.take(6)})" else "Auth Empty"
                    _homeUiState.update { it.copy(authStatus = status) }
                }.onFailure { error ->
                    _homeUiState.update { it.copy(authStatus = "Error: ${error.message?.take(20)}...") }
                    android.util.Log.e("FinancialViewModel", "Auth failed", error)
                }
            }

            // Check local DB (Room) via a simple query
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

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            
            // Update account balance(s)
            val currentAccounts = _homeUiState.value.accounts
            
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { account ->
                        val newBalance = parseBalance(account.balance) - transaction.amount
                        repository.updateAccount(account.copy(balance = formatBalance(newBalance)))
                    }
                }
                TransactionType.INCOME -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { account ->
                        val newBalance = parseBalance(account.balance) + transaction.amount
                        repository.updateAccount(account.copy(balance = formatBalance(newBalance)))
                    }
                }
                TransactionType.TRANSFER -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { fromAcc ->
                        val newFromBalance = parseBalance(fromAcc.balance) - transaction.amount
                        repository.updateAccount(fromAcc.copy(balance = formatBalance(newFromBalance)))
                    }
                    transaction.toAccountId?.let { toId ->
                        currentAccounts.find { it.id == toId }?.let { toAcc ->
                            val newToBalance = parseBalance(toAcc.balance) + transaction.amount
                            repository.updateAccount(toAcc.copy(balance = formatBalance(newToBalance)))
                        }
                    }
                }
                TransactionType.ADJUSTMENT -> {
                    currentAccounts.find { it.id == transaction.fromAccountId }?.let { account ->
                        repository.updateAccount(account.copy(balance = formatBalance(transaction.amount)))
                    }
                }
                else -> {} // Handle others later
            }
        }
    }

    fun addCreditAccount(
        name: String,
        balance: String,
        creditLimit: String,
        iconUri: String?,
        statementCloseDay: String,
        autoClear: Boolean,
        additionalInfo: String,
        groupId: String?,
        monitoredByBudgetId: String? = null
    ) {
        viewModelScope.launch {
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                balance = balance,
                type = AccountType.CREDIT,
                color = androidx.compose.ui.graphics.Color(0xFFE91E63), // CreditPrimaryColor
                iconUri = iconUri,
                creditLimit = creditLimit,
                statementCloseDay = statementCloseDay,
                autoClear = autoClear,
                additionalInfo = additionalInfo,
                groupId = groupId,
                monitoredByBudgetId = monitoredByBudgetId
            )
            repository.addAccount(account)
        }
    }

    fun addLoanAccount(
        name: String,
        principal: String,
        apr: String,
        duration: String,
        startDate: String,
        firstDueDate: String,
        groupId: String?,
        additionalInfo: String,
        monitoredByBudgetId: String? = null
    ) {
        viewModelScope.launch {
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                balance = "-$principal", // Loan starts as negative
                type = AccountType.LOAN,
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                principalAmount = principal,
                apr = apr,
                duration = duration,
                startDate = startDate,
                firstDueDate = firstDueDate,
                groupId = groupId,
                additionalInfo = additionalInfo,
                monitoredByBudgetId = monitoredByBudgetId
            )
            repository.addAccount(account)
        }
    }

    fun addInvestmentAccount(
        name: String,
        balance: String,
        date: String,
        groupId: String?,
        additionalInfo: String,
        monitoredByBudgetId: String? = null
    ) {
        viewModelScope.launch {
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                balance = balance,
                type = AccountType.INVESTMENT,
                color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                asOfDate = date,
                groupId = groupId,
                additionalInfo = additionalInfo,
                monitoredByBudgetId = monitoredByBudgetId
            )
            repository.addAccount(account)
        }
    }

    fun addForexAccount(
        name: String,
        currency: String,
        groupId: String?,
        additionalInfo: String,
        monitoredByBudgetId: String? = null
    ) {
        viewModelScope.launch {
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                balance = "0.00",
                type = AccountType.FOREX,
                color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                currency = currency,
                groupId = groupId,
                additionalInfo = additionalInfo,
                monitoredByBudgetId = monitoredByBudgetId
            )
            repository.addAccount(account)
        }
    }

    fun addStandardAccount(
        name: String,
        balance: String,
        type: AccountType,
        groupId: String?,
        autoClear: Boolean,
        additionalInfo: String,
        monitoredByBudgetId: String? = null
    ) {
        viewModelScope.launch {
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                balance = balance,
                type = type,
                color = androidx.compose.ui.graphics.Color(0xFF9C27B0),
                groupId = groupId,
                autoClear = autoClear,
                additionalInfo = additionalInfo,
                monitoredByBudgetId = monitoredByBudgetId
            )
            repository.addAccount(account)
        }
    }

    private fun parseBalance(balance: String): Double {
        return try {
            // Normalize separators: handle both dots and commas. 
            // If both exist, the last one is likely the decimal separator.
            // For simple case like "5,00" or "5.00", we convert comma to dot.
            val normalized = balance.replace(",", ".")
            // Remove everything except digits, dots and minus sign
            // If there are multiple dots, keep only the last one as decimal
            val regex = Regex("[^0-9.-]")
            val clean = normalized.replace(regex, "")
            
            val lastDotIndex = clean.lastIndexOf('.')
            if (lastDotIndex != -1) {
                val integerPart = clean.substring(0, lastDotIndex).replace(".", "")
                val fractionalPart = clean.substring(lastDotIndex + 1)
                (integerPart + "." + fractionalPart).toDouble()
            } else {
                clean.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun formatBalance(balance: Double): String {
        return java.util.Locale.getDefault().let { locale ->
            String.format(locale, "$%.2f", balance)
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getBalanceData(),
                repository.getAccounts(),
                repository.getAccountGroups(),
                repository.getBudgets(),
                repository.getBudgetGroups(),
                repository.getTransactions()
            ) { args: Array<Any> ->
                stateUpdate(
                    args[0] as BalanceData,
                    args[1] as List<Account>,
                    args[2] as List<AccountGroup>,
                    args[3] as List<Budget>,
                    args[4] as List<BudgetGroup>,
                    args[5] as List<Transaction>
                )
            }.flowOn(Dispatchers.IO)
                .collect { update ->
                    _homeUiState.update { currentState ->
                        update(currentState)
                    }
                }
        }
    }

    private fun stateUpdate(
        balance: BalanceData,
        accounts: List<Account>,
        accountGroups: List<AccountGroup>,
        budgets: List<Budget>,
        budgetGroups: List<BudgetGroup>,
        transactions: List<Transaction>
    ): (HomeUiState) -> HomeUiState = { currentState ->
        val totalBudgeted = budgets.filter { !it.isIncome }.sumOf { it.amount }
        val totalSpent = budgets.filter { !it.isIncome }.sumOf { it.spent }
        val remainingBudget = totalBudgeted - totalSpent

        currentState.copy(
            balanceData = balance,
            accounts = accounts,
            accountGroups = accountGroups,
            budgets = budgets,
            budgetGroups = budgetGroups,
            transactions = transactions,
            totalBudgeted = formatBalance(totalBudgeted),
            remainingBudget = formatBalance(remainingBudget),
            isLoading = false
        )
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            repository.getCategorySpending()
                .flowOn(Dispatchers.IO)
                .collect { categories ->
                    _statsUiState.update { it.copy(categories = categories, isLoading = false) }
                }
        }
    }
}
