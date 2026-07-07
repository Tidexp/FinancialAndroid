package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.domain.model.*
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
    private val authRepository: AuthRepository
) {
    private val userId: String get() = authRepository.currentUser?.id ?: "anonymous"
    private val isAnonymous: Boolean get() = authRepository.currentUser?.isAnonymous ?: true

    private fun <T> syncToFirestore(collection: String, id: String, data: T) {
        if (isAnonymous) return
        firestore.collection("users").document(userId)
            .collection(collection).document(id).set(data!!)
    }

    private fun deleteFromFirestore(collection: String, id: String) {
        if (isAnonymous) return
        firestore.collection("users").document(userId)
            .collection(collection).document(id).delete()
    }

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

    fun getCategorySpending(): Flow<List<CategorySpending>> = getTransactions().map { transactions ->
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE && it.budgetId == null }
        val totalExpense = expenseTransactions.sumOf { it.amount }
        
        expenseTransactions.groupBy { it.categoryId ?: "Uncategorized" }
            .map { (category, list) ->
                val amount = list.sumOf { it.amount }
                CategorySpending(
                    label = category,
                    amount = formatBalance(amount),
                    progress = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f,
                    color = androidx.compose.ui.graphics.Color((0xFF000000.toLong() or (category.hashCode().toLong() and 0xFFFFFF)).toInt())
                )
            }.sortedByDescending { it.progress }
    }.flowOn(Dispatchers.Default)

    fun getReportsData(startDate: Long, endDate: Long): Flow<ReportsData> = getTransactions().map { allTransactions ->
        val filtered = allTransactions.filter { it.date in startDate..endDate && it.budgetId == null }
        
        val totalIncome = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        
        val expenseByCategory = filtered.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId ?: "Uncategorized" }
            .map { (cat, list) ->
                val amt = list.sumOf { it.amount }
                CategorySpending(cat, formatBalance(amt), if(totalExpense > 0) (amt/totalExpense).toFloat() else 0f, 
                    androidx.compose.ui.graphics.Color((0xFF000000.toLong() or (cat.hashCode().toLong() and 0xFFFFFF)).toInt()))
            }.sortedByDescending { it.progress }

        val incomeByCategory = filtered.filter { it.type == TransactionType.INCOME }
            .groupBy { it.categoryId ?: "Uncategorized" }
            .map { (cat, list) ->
                val amt = list.sumOf { it.amount }
                CategorySpending(cat, formatBalance(amt), if(totalIncome > 0) (amt/totalIncome).toFloat() else 0f,
                    androidx.compose.ui.graphics.Color((0xFF000000.toLong() or (cat.hashCode().toLong() and 0xFFFFFF)).toInt()))
            }.sortedByDescending { it.progress }

        // Group by day for trend
        val dailyTrend = filtered.groupBy { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.map { (date, list) ->
            DailyAmount(
                date = date,
                income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }.sortedBy { it.date }

        ReportsData(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netCashFlow = totalIncome - totalExpense,
            categorySpending = expenseByCategory,
            incomeByCategory = incomeByCategory,
            dailyTrend = dailyTrend
        )
    }.flowOn(Dispatchers.Default)

    suspend fun addTransaction(t: Transaction) { 
        val entity = t.toEntity(userId, false)
        transactionDao.insertTransaction(entity) 
        syncToFirestore("transactions", t.id, entity)
    }
    suspend fun updateTransaction(t: Transaction) { 
        val entity = t.toEntity(userId, false)
        transactionDao.insertTransaction(entity) 
        syncToFirestore("transactions", t.id, entity)
    }
    suspend fun deleteTransaction(t: Transaction) { 
        transactionDao.deleteTransaction(t.toEntity(userId, false)) 
        deleteFromFirestore("transactions", t.id)
    }
    suspend fun addAccount(a: Account) { 
        val entity = a.toEntity(userId, false)
        accountDao.insertAccount(entity) 
        syncToFirestore("accounts", a.id, entity)
    }
    suspend fun updateAccount(a: Account) { 
        val entity = a.toEntity(userId, false)
        accountDao.updateAccount(entity) 
        syncToFirestore("accounts", a.id, entity)
    }
    suspend fun deleteAccount(a: Account) { 
        accountDao.deleteAccount(a.toEntity(userId, false)) 
        deleteFromFirestore("accounts", a.id)
    }
    suspend fun addAccountGroup(g: AccountGroup) { 
        val entity = g.toEntity(userId, false)
        accountGroupDao.insertGroup(entity) 
        syncToFirestore("accountGroups", g.id, entity)
    }
    suspend fun updateAccountGroup(g: AccountGroup) { 
        val entity = g.toEntity(userId, false)
        accountGroupDao.updateGroup(entity) 
        syncToFirestore("accountGroups", g.id, entity)
    }
    suspend fun deleteAccountGroup(g: AccountGroup) { 
        accountGroupDao.deleteGroup(g.toEntity(userId, false)) 
        deleteFromFirestore("accountGroups", g.id)
    }
    suspend fun addBudget(b: Budget) { 
        val entity = b.toEntity(userId, false)
        budgetDao.insertBudget(entity) 
        syncToFirestore("budgets", b.id, entity)
    }
    suspend fun updateBudget(b: Budget) { 
        val entity = b.toEntity(userId, false)
        budgetDao.updateBudget(entity) 
        syncToFirestore("budgets", b.id, entity)
    }
    suspend fun deleteBudget(b: Budget) { 
        budgetDao.deleteBudget(b.toEntity(userId, false)) 
        deleteFromFirestore("budgets", b.id)
    }
    suspend fun addBudgetGroup(g: BudgetGroup) { 
        val entity = g.toEntity(userId, false)
        budgetDao.insertBudgetGroup(entity) 
        syncToFirestore("budgetGroups", g.id, entity)
    }
    suspend fun updateBudgetGroup(g: BudgetGroup) { 
        val entity = g.toEntity(userId, false)
        budgetDao.updateBudgetGroup(entity) 
        syncToFirestore("budgetGroups", g.id, entity)
    }
    suspend fun deleteBudgetGroup(g: BudgetGroup) { 
        budgetDao.deleteBudgetGroup(g.toEntity(userId, false)) 
        deleteFromFirestore("budgetGroups", g.id)
    }
}
