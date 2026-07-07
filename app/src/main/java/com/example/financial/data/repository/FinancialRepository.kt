package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.Calendar

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val budgetDao: com.example.financial.data.local.dao.BudgetDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String get() = auth.currentUser?.uid ?: "anonymous"

    fun getTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getAccounts(): Flow<List<Account>> = accountDao.getAllAccounts().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getAccountGroups(): Flow<List<AccountGroup>> = accountGroupDao.getAllGroups().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getBudgetGroups(): Flow<List<BudgetGroup>> = budgetDao.getAllBudgetGroups().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    fun getBudgets(): Flow<List<Budget>> = combine(budgetDao.getAllBudgets(), getTransactions()) { entities, allTransactions ->
        entities.map { entity ->
            val budget = entity.toDomain()
            // Lọc giao dịch: Hoặc là giao dịch thật khớp account/category, hoặc là giao dịch ảo dành riêng cho budget này
            val relevantTransactions = allTransactions.filter { t ->
                if (t.budgetId == budget.id) return@filter true // Giao dịch ảo của budget
                if (t.budgetId != null) return@filter false // Giao dịch ảo của budget khác
                
                // Giao dịch thật
                val typeMatch = t.type == (if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE)
                val accMatch = budget.accountIds.isEmpty() || budget.accountIds.contains(t.fromAccountId)
                val catMatch = budget.categories.isEmpty() || budget.categories.any { it.equals(t.payee, true) || it.equals(t.description, true) }
                typeMatch && accMatch && catMatch
            }

            val now = System.currentTimeMillis()
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

            budget.copy(spent = spentInCurrentPeriod, remaining = budget.amount - spentInCurrentPeriod + rollover, progress = if (budget.amount > 0) (spentInCurrentPeriod / budget.amount).toFloat().coerceIn(0f, 1f) else 0f)
        }
    }.flowOn(Dispatchers.Default)

    fun getBalanceData(): Flow<BalanceData> = combine(getAccounts(), getTransactions()) { accounts, transactions ->
        // Ở đây CHỈ lấy giao dịch thật (budgetId == null)
        val realTransactions = transactions.filter { it.budgetId == null }
        var total = 0.0; var liab = 0.0
        accounts.forEach { a -> val b = parseBalance(a.balance); if (b < 0) liab += kotlin.math.abs(b); total += b }
        val inc = realTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val exp = realTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        BalanceData(netWorth = formatBalance(total), liabilities = formatBalance(-liab), totalIncome = formatBalance(inc), totalExpenses = formatBalance(exp), monthlyBudget = 0f)
    }.flowOn(Dispatchers.Default)

    private fun parseBalance(s: String): Double = try { s.replace(",", ".").replace(Regex("[^0-9.-]"), "").toDouble() } catch (e: Exception) { 0.0 }
    private fun formatBalance(d: Double): String = String.format(java.util.Locale.getDefault(), "$%.2f", d)

    fun getCategorySpending(): Flow<List<CategorySpending>> = flowOf(emptyList())

    suspend fun addTransaction(t: Transaction) { transactionDao.insertTransaction(t.toEntity()) }
    suspend fun updateTransaction(t: Transaction) { transactionDao.insertTransaction(t.toEntity()) }
    suspend fun deleteTransaction(t: Transaction) { transactionDao.deleteTransaction(t.toEntity()) }
    suspend fun addAccount(a: Account) { accountDao.insertAccount(a.toEntity()) }
    suspend fun updateAccount(a: Account) { accountDao.updateAccount(a.toEntity()) }
    suspend fun deleteAccount(a: Account) { accountDao.deleteAccount(a.toEntity()) }
    suspend fun addAccountGroup(g: AccountGroup) { accountGroupDao.insertGroup(g.toEntity()) }
    suspend fun updateAccountGroup(g: AccountGroup) { accountGroupDao.updateGroup(g.toEntity()) }
    suspend fun deleteAccountGroup(g: AccountGroup) { accountGroupDao.deleteGroup(g.toEntity()) }
    suspend fun addBudget(b: Budget) { budgetDao.insertBudget(b.toEntity()) }
    suspend fun updateBudget(b: Budget) { budgetDao.updateBudget(b.toEntity()) }
    suspend fun deleteBudget(b: Budget) { budgetDao.deleteBudget(b.toEntity()) }
    suspend fun addBudgetGroup(g: BudgetGroup) { budgetDao.insertBudgetGroup(g.toEntity()) }
    suspend fun updateBudgetGroup(g: BudgetGroup) { budgetDao.updateBudgetGroup(g.toEntity()) }
    suspend fun deleteBudgetGroup(g: BudgetGroup) { budgetDao.deleteBudgetGroup(g.toEntity()) }
}
